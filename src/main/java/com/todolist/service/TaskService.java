package com.todolist.service;

import com.todolist.dto.OnUpdate;
import com.todolist.dto.TaskUpdateDto;
import com.todolist.exception.TaskNotFoundException;
import com.todolist.mapper.TaskMapper;
import com.todolist.model.Priority;
import com.todolist.model.Task;
import com.todolist.repository.TaskRepository;
import com.todolist.scope.PrototypeScopedBean;
import com.todolist.validation.DueDateNotBeforeCreationValidator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskService {
  private static final Logger log = LoggerFactory.getLogger(TaskService.class);

  private final TaskRepository repo;
  private final TaskMapper mapper;
  private final Validator validator;
  private final ObjectProvider<PrototypeScopedBean> prototypeScopedBeanProvider;

  private final Map<String, Task> taskCache = new ConcurrentHashMap<>();

  public TaskService(
      TaskRepository repo,
      TaskMapper mapper,
      Validator validator,
      ObjectProvider<PrototypeScopedBean> prototypeScopedBeanProvider
  ) {
    this.repo = repo;
    this.mapper = mapper;
    this.validator = validator;
    this.prototypeScopedBeanProvider = prototypeScopedBeanProvider;
  }

  @PostConstruct
  void init() {
    List<Task> tasks = repo.findAll();
    if (tasks.isEmpty()) {
      seedDefaultTasks();
      tasks = repo.findAll();
    }

    taskCache.clear();
    for (Task t : tasks) {
      if (t.getId() != null) {
        taskCache.put(String.valueOf(t.getId()), t);
      }
    }

    log.info("TaskService initialized. Cache size={}", taskCache.size());
  }

  @PreDestroy
  void destroy() {
    log.info("TaskService is being destroyed. Cache size={}", taskCache.size());
    taskCache.clear();
  }

  public List<Task> all() {
    return new ArrayList<>(taskCache.values());
  }

  public Task get(Long id) {
    String key = String.valueOf(id);
    Task task = taskCache.get(key);
    if (task == null) {
      throw new TaskNotFoundException();
    }
    return task;
  }

  public Task create(Task t) {
    if (t.getCreatedAt() == null) {
      t.setCreatedAt(LocalDateTime.now());
    }
    if (t.getId() == null) {
      t.setId(prototypeScopedBeanProvider.getObject().generateTaskId());
    }

    Task saved = repo.save(t);
    if (saved.getId() != null) {
      taskCache.put(String.valueOf(saved.getId()), saved);
    }
    return saved;
  }

  public Task update(Long id, TaskUpdateDto dto) {
    Task task = get(id);

    if (dto.getDueDate() != null) {
      LocalDate createdAtDate = task.getCreatedAt().toLocalDate();
      DueDateNotBeforeCreationValidator.setCreatedAtOverride(createdAtDate);
      try {
        Set<ConstraintViolation<TaskUpdateDto>> violations = validator.validate(dto, OnUpdate.class);
        if (!violations.isEmpty()) {
          throw new ConstraintViolationException(violations);
        }
      } finally {
        DueDateNotBeforeCreationValidator.clearCreatedAtOverride();
      }
    }

    mapper.update(dto, task);
    Task saved = repo.save(task);
    if (saved.getId() != null) {
      taskCache.put(String.valueOf(saved.getId()), saved);
    }
    return saved;
  }

  public void delete(Long id) {
    String key = String.valueOf(id);
    if (!taskCache.containsKey(key)) {
      throw new TaskNotFoundException();
    }
    repo.delete(id);
    taskCache.remove(key);
  }

  private void seedDefaultTasks() {
    LocalDateTime now = LocalDateTime.now();

    Task t1 = new Task();
    t1.setTitle("Task 1");
    t1.setDescription("Seed task 1");
    t1.setCompleted(false);
    t1.setCreatedAt(now.minusDays(2));
    t1.setDueDate(t1.getCreatedAt().toLocalDate().plusDays(2));
    t1.setPriority(Priority.LOW);
    t1.setTags(new HashSet<>(List.of("seed")));
    repo.save(t1);

    Task t2 = new Task();
    t2.setTitle("Task 2");
    t2.setDescription("Seed task 2");
    t2.setCompleted(true);
    t2.setCreatedAt(now.minusDays(1));
    t2.setDueDate(t2.getCreatedAt().toLocalDate().plusDays(1));
    t2.setPriority(Priority.MEDIUM);
    t2.setTags(new HashSet<>(List.of("seed", "demo")));
    repo.save(t2);
  }
}