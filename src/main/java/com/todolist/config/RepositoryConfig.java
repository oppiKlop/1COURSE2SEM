package com.todolist.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.todolist.repository.StubTaskRepository;
import com.todolist.repository.TaskRepository;

@Configuration
public class RepositoryConfig {

    @Bean
    @Qualifier("repositoryStub")
    public TaskRepository stubRepository() {
        return new StubTaskRepository();
    }
}