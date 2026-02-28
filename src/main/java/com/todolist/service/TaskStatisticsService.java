package com.todolist.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.todolist.repository.TaskRepository;

@Service
public class TaskStatisticsService {

    private final TaskRepository taskRepositoryInMemory;
    private final TaskRepository taskRepositoryStub;

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Value("${server.port}")
    private String serverPort;

    public TaskStatisticsService(TaskRepository taskRepositoryInMemory,
                                 @Qualifier("repositoryStub") TaskRepository taskRepositoryStub) {
        this.taskRepositoryInMemory = taskRepositoryInMemory;
        this.taskRepositoryStub = taskRepositoryStub;
    }

    public String compareBeanInfo() {
        return "Primary bean: " + taskRepositoryInMemory + "\n"
                + "Qualifier bean: " + taskRepositoryStub;
    }

    public String getAppInfo() {
        return "App: " + appName + " " + appVersion +  " running on port: " + serverPort;
    }
}