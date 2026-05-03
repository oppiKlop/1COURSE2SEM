package com.todolist.api;

import com.todolist.dto.ExternalTaskCreateRequest;
import com.todolist.dto.ExternalTaskDto;
import com.todolist.service.TasksGatewayService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
public class GatewayTasksController {
  private final TasksGatewayService tasksGatewayService;

  public GatewayTasksController(TasksGatewayService tasksGatewayService) {
    this.tasksGatewayService = tasksGatewayService;
  }

  @PostMapping
  public ResponseEntity<ExternalTaskDto> create(@Valid @RequestBody ExternalTaskCreateRequest request) {
    ExternalTaskDto created = tasksGatewayService.createTask(request);
    URI location = URI.create("/api/v1/tasks/" + created.getId());
    return ResponseEntity.created(location).body(created);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ExternalTaskDto> get(@PathVariable Long id) {
    return ResponseEntity.ok(tasksGatewayService.getTask(id));
  }

  @GetMapping
  public ResponseEntity<List<ExternalTaskDto>> list(
      @RequestParam(required = false) Boolean completed,
      @RequestParam(required = false) Integer limit
  ) {
    return ResponseEntity.ok(tasksGatewayService.listTasks(completed, limit));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    tasksGatewayService.deleteTask(id);
    return ResponseEntity.noContent().build();
  }
}
