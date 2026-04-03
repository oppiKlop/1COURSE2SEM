package com.todolist.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.todolist.repository.TaskRepository;

import java.util.Map;

@Service
public class TaskStatisticsService {

    private final TaskRepository primary;
    private final TaskRepository stub;

    public TaskStatisticsService(
            TaskRepository primary,
            @Qualifier("stubTaskRepository") TaskRepository stub) {

        this.primary = primary;
        this.stub = stub;
    }

    public Map<String, Integer> stats() {
        return Map.of(
                "primary", primary.findAll().size(),
                "stub", stub.findAll().size()
        );
    }
}