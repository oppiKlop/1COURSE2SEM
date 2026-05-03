package com.todolist.service;

import com.todolist.client.ExternalTasksClient;
import com.todolist.dto.ExternalTaskCreateRequest;
import com.todolist.dto.ExternalTaskDto;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TasksGatewayService {
  private final ExternalTasksClient externalTasksClient;

  public TasksGatewayService(ExternalTasksClient externalTasksClient) {
    this.externalTasksClient = externalTasksClient;
  }

  @RateLimiter(name = "externalApi")
  public ExternalTaskDto createTask(ExternalTaskCreateRequest request) {
    return externalTasksClient.create(request);
  }

  @RateLimiter(name = "externalApi")
  public ExternalTaskDto getTask(Long id) {
    return externalTasksClient.getById(id);
  }

  @RateLimiter(name = "externalApi")
  public List<ExternalTaskDto> listTasks(Boolean completed, Integer limit) {
    return externalTasksClient.list(completed, limit);
  }

  @RateLimiter(name = "externalApi")
  public void deleteTask(Long id) {
    externalTasksClient.delete(id);
  }
}
