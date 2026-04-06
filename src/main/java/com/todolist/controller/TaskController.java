package com.todolist.controller;

import com.todolist.dto.*;
import com.todolist.mapper.TaskMapper;
import com.todolist.model.Task;
import com.todolist.service.TaskService;
import com.todolist.scope.RequestScopedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final TaskService service;
    private final TaskMapper mapper;
    private final RequestScopedBean requestScopedBean;

    @Value("${app.version}")
    private String version;

    public TaskController(TaskService s, TaskMapper m, RequestScopedBean requestScopedBean) {
        this.service = s;
        this.mapper = m;
        this.requestScopedBean = requestScopedBean;
    }

    @GetMapping
    @Operation(summary = "Get all tasks")
    @ApiResponse(responseCode = "200", description = "Tasks returned")
    public ResponseEntity<List<TaskResponseDto>> all() {
        log.debug("RequestScopedBean requestId={}", requestScopedBean.getRequestId());

        List<Task> tasks = service.all();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(tasks.size()))
                .header("X-API-Version", version)
                .body(tasks.stream().map(mapper::toDto).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by id")
    @ApiResponse(responseCode = "200", description = "Task returned")
    public ResponseEntity<TaskResponseDto> get(@PathVariable Long id) {
        log.debug("RequestScopedBean requestId={}", requestScopedBean.getRequestId());
        return ResponseEntity.ok()
                .header("X-API-Version", version)
                .body(mapper.toDto(service.get(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new task")
    @ApiResponse(responseCode = "201", description = "Task created")
    public ResponseEntity<TaskResponseDto> create(
            @Validated(OnCreate.class) @RequestBody TaskCreateDto dto) {
        log.debug("RequestScopedBean requestId={}", requestScopedBean.getRequestId());

        Task t = mapper.toEntity(dto);
        return ResponseEntity.status(201)
                .header("X-API-Version", version)
                .body(mapper.toDto(service.create(t)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing task")
    @ApiResponse(responseCode = "200", description = "Task updated")
    public ResponseEntity<TaskResponseDto> update(
            @PathVariable Long id,
            @Validated(OnUpdate.class) @RequestBody TaskUpdateDto dto) {
        log.debug("RequestScopedBean requestId={}", requestScopedBean.getRequestId());

        return ResponseEntity.ok()
                .header("X-API-Version", version)
                .body(mapper.toDto(service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    @ApiResponse(responseCode = "204", description = "Task deleted")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("RequestScopedBean requestId={}", requestScopedBean.getRequestId());
        service.delete(id);
        return ResponseEntity.noContent()
                .header("X-API-Version", version)
                .build();
    }
}