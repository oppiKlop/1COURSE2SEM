package com.todolist.external;

import com.todolist.dto.ExternalTaskCreateRequest;
import com.todolist.dto.ExternalTaskDto;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/external/v1")
public class ExternalApiController {
  private final Map<Long, ExternalTaskDto> store = new ConcurrentHashMap<>();
  private final AtomicLong idSeq = new AtomicLong(1000);

  @PostMapping("/tasks")
  public ResponseEntity<ExternalTaskDto> createTask(@RequestBody ExternalTaskCreateRequest request) {
    long id = idSeq.incrementAndGet();
    ExternalTaskDto task = new ExternalTaskDto();
    task.setId(id);
    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setCompleted(request.isCompleted());
    store.put(id, task);

    return ResponseEntity
        .created(URI.create("/external/v1/tasks/" + id))
        .body(task);
  }

  @GetMapping("/tasks/{id}")
  public ResponseEntity<?> getTask(@PathVariable Long id) {
    ExternalTaskDto task = store.get(id);
    if (task == null) {
      ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Task " + id + " not found");
      pd.setTitle("Not Found");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }
    return ResponseEntity.ok(task);
  }

  @GetMapping("/tasks")
  public ResponseEntity<List<ExternalTaskDto>> listTasks(
      @RequestParam(required = false) Boolean completed,
      @RequestParam(required = false) Integer limit
  ) {
    List<ExternalTaskDto> tasks = new ArrayList<>(store.values());
    tasks.sort(Comparator.comparing(ExternalTaskDto::getId));
    if (completed != null) {
      tasks = tasks.stream().filter(t -> t.isCompleted() == completed).toList();
    }
    if (limit != null && limit > 0 && tasks.size() > limit) {
      tasks = tasks.subList(0, limit);
    }
    return ResponseEntity.ok(tasks);
  }

  @PutMapping("/tasks/{id}")
  public ResponseEntity<?> updateTask(
      @PathVariable Long id,
      @RequestBody ExternalTaskCreateRequest request
  ) {
    ExternalTaskDto existing = store.get(id);
    if (existing == null) {
      ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Task " + id + " not found");
      pd.setTitle("Not Found");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }
    existing.setTitle(request.getTitle());
    existing.setDescription(request.getDescription());
    existing.setCompleted(request.isCompleted());
    return ResponseEntity.ok(existing);
  }

  @DeleteMapping("/tasks/{id}")
  public ResponseEntity<?> deleteTask(@PathVariable Long id) {
    ExternalTaskDto removed = store.remove(id);
    if (removed == null) {
      ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Task " + id + " not found");
      pd.setTitle("Not Found");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/unstable")
  public ResponseEntity<?> unstable(@RequestParam String mode) throws InterruptedException {
    return switch (mode) {
      case "timeout" -> {
        Thread.sleep(Duration.ofSeconds(4).toMillis());
        yield ResponseEntity.ok(Map.of("mode", "timeout", "status", "eventual-response"));
      }
      case "500" -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Simulated 500"));
      case "429" -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
          .header(HttpHeaders.RETRY_AFTER, "2")
          .body(ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, "Too many requests"));
      case "html" -> ResponseEntity.status(HttpStatus.BAD_GATEWAY)
          .contentType(MediaType.TEXT_HTML)
          .body("<html><body><h1>Bad gateway upstream</h1></body></html>");
      default -> ResponseEntity.badRequest().body(Map.of("error", "Unknown mode"));
    };
  }
}
