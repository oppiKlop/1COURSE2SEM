package com.todolist.service;

import com.todolist.mapper.TaskMapper;
import com.todolist.model.Task;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.todolist.repository.TaskRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    private final TaskMapper mapper;

    private final Map<Long, Task> taskCache = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    @PostConstruct
    public void init() {
        for (Task task : taskRepository.getAllTasks()) {
            taskCache.put(task.getId(), task);
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("Кол-во задач в кэше: " + taskCache.size());
    }


    public TaskService(TaskRepository taskRepository, TaskMapper mapper) {
        this.taskRepository = taskRepository;
        this.mapper = mapper;
    }

    public List<Task> getAllTasks() {
        return taskRepository.getAllTasks();
    }

    public Task getTask(Long id) {
        Optional<Task> taskOpt = taskRepository.getTask(id);
        if (taskOpt.isEmpty()) {
            throw new RuntimeException("Not found task with id: " + id);
        }
        return taskOpt.get();
    }

    public Task insertTask(String description, String title) {
        return taskRepository.insertTask(description, title);
    }

    public Task updateTask(Long id, String description, String title, boolean completed) {
        return taskRepository.updateTask(id, description, title, completed);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteTask(id);
    }
}