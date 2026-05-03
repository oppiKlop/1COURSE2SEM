package com.todolist.controller;

import com.todolist.dto.TaskResponseDto;
import com.todolist.mapper.TaskMapper;
import com.todolist.exception.TaskNotFoundException;
import com.todolist.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/favorites")
public class FavoritesController {

  private final TaskService taskService;
  private final TaskMapper mapper;
  private final String apiVersion;

  public FavoritesController(TaskService taskService, TaskMapper mapper, @Value("${app.version}") String apiVersion) {
    this.taskService = taskService;
    this.mapper = mapper;
    this.apiVersion = apiVersion;
  }

  @PostMapping("/{id}")
  @Operation(summary = "Add task to favorites")
  @ApiResponse(responseCode = "200", description = "Favorite added")
  public ResponseEntity<Void> add(@PathVariable("id") Long id, HttpSession s) {
    List<Long> list = (List<Long>) s.getAttribute("favoriteTaskIds");
    if (list == null) {
      list = new ArrayList<>();
    }
    // Сессия может содержать неизменяемый список (например, List.of в тестах),
    // поэтому создаём копию перед модификацией.
    list = new ArrayList<>(list);
    if (!list.contains(id)) {
      list.add(id);
      s.setAttribute("favoriteTaskIds", list);
    }
    return ResponseEntity.ok().header("X-API-Version", apiVersion).build();
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remove task from favorites")
  @ApiResponse(responseCode = "204", description = "Favorite removed")
  public ResponseEntity<Void> delete(@PathVariable("id") Long id, HttpSession s) {
    List<Long> list = (List<Long>) s.getAttribute("favoriteTaskIds");
    if (list == null) {
      return ResponseEntity.noContent().header("X-API-Version", apiVersion).build();
    }
    List<Long> updated = new ArrayList<>(list);
    updated.remove(id);
    s.setAttribute("favoriteTaskIds", updated);
    return ResponseEntity.noContent().header("X-API-Version", apiVersion).build();
  }

  @GetMapping
  @Operation(summary = "Get favorite tasks")
  @ApiResponse(responseCode = "200", description = "Favorites returned")
  public ResponseEntity<List<TaskResponseDto>> list(HttpSession s) {
    List<Long> ids = (List<Long>) s.getAttribute("favoriteTaskIds");
    if (ids == null) {
      ids = List.of();
    }

    List<TaskResponseDto> tasks = ids.stream()
        .map(id -> {
          try {
            return mapper.toDto(taskService.get(id));
          } catch (TaskNotFoundException ex) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .toList();

    return ResponseEntity.ok()
        .header("X-API-Version", apiVersion)
        .body(tasks);
  }
}
