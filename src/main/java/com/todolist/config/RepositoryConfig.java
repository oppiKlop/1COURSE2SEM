package com.todolist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.todolist.repository.StubTaskRepository;
import com.todolist.repository.TaskRepository;

@Configuration
public class RepositoryConfig {

  @Bean("stubTaskRepository")
  public TaskRepository stubRepository() {
    return new StubTaskRepository();
  }
}