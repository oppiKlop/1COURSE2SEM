package com.todolist.controller;

import com.todolist.dto.*;
import com.todolist.mapper.TaskMapper;
import com.todolist.model.Task;
import com.todolist.service.TaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;
    private final TaskMapper mapper;

    @Value("${app.version}")
    private String version;

    public TaskController(TaskService s, TaskMapper m) {
        this.service = s;
        this.mapper = m;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> all() {

        List<Task> tasks = service.all();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(tasks.size()))
                .header("X-API-Version", version)
                .body(tasks.stream().map(mapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(service.get(id)));
    }

    @PostMapping
    public ResponseEntity<TaskResponseDto> create(
            @Validated(OnCreate.class) @RequestBody TaskCreateDto dto) {

        Task t = mapper.toEntity(dto);
        return ResponseEntity.status(201)
                .body(mapper.toDto(service.create(t)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> update(
            @PathVariable Long id,
            @Validated(OnUpdate.class) @RequestBody TaskUpdateDto dto) {

        return ResponseEntity.ok(
                mapper.toDto(service.update(id, dto, mapper))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}