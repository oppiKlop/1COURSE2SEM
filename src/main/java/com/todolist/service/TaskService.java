package com.todolist.service;

import com.todolist.dto.TaskUpdateDto;
import com.todolist.exception.TaskNotFoundException;
import com.todolist.mapper.TaskMapper;
import com.todolist.model.Task;
import com.todolist.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository repo;

    public TaskService(TaskRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    void init() {
        System.out.println("INIT");
    }

    @PreDestroy
    void destroy() {
        System.out.println("DESTROY");
    }

    public List<Task> all() {
        return repo.findAll();
    }

    public Task get(Long id) {
        return repo.findById(id)
                .orElseThrow(TaskNotFoundException::new);
    }

    public Task create(Task t) {
        t.setCreatedAt(LocalDateTime.now());
        return repo.save(t);
    }

    public Task update(Long id, TaskUpdateDto dto, TaskMapper mapper) {
        Task t = get(id);
        mapper.update(dto, t);
        return repo.save(t);
    }

    public void delete(Long id) {
        repo.delete(id);
    }
}