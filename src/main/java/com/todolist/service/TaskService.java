package com.todolist.service;

import com.todolist.dto.TaskCreateDto;
import com.todolist.dto.TaskUpdateDto;
import com.todolist.mapper.TaskMapper;
import com.todolist.model.Task;
import com.todolist.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper mapper;

    public TaskService(TaskRepository taskRepository, TaskMapper mapper) {
        this.taskRepository = taskRepository;
        this.mapper = mapper;
    }

    public List<Task> getAllTasks() {
        return taskRepository.getAllTasks();
    }

    public Task getTask(Long id) {
        return taskRepository.getTask(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public Task create(TaskCreateDto dto) {
        Task task = mapper.toEntity(dto);
        task.setCreatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public Task update(Long id, TaskUpdateDto dto) {
        Task task = getTask(id);

        mapper.updateEntity(dto, task);

        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteTask(id);
    }
}