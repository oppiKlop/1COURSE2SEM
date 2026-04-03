package com.todolist.repository;

import com.todolist.model.Task;

import com.todolist.model.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class StubTaskRepository implements TaskRepository {
  private final Map<Long, Task> stubTasks = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  public StubTaskRepository() {
    seedIfEmpty();
  }

  private void seedIfEmpty() {
    if (!stubTasks.isEmpty()) {
      return;
    }

    LocalDateTime created1 = LocalDateTime.now().minusDays(2);
    LocalDate due1 = created1.toLocalDate().plusDays(3);
    Task t1 = new Task();
    t1.setId(idGenerator.getAndIncrement());
    t1.setTitle("Stub task 1");
    t1.setDescription("First stub task");
    t1.setCompleted(false);
    t1.setCreatedAt(created1);
    t1.setDueDate(due1);
    t1.setPriority(Priority.MEDIUM);
    t1.setTags(new HashSet<>(List.of("work")));
    stubTasks.put(t1.getId(), t1);

    LocalDateTime created2 = LocalDateTime.now().minusDays(1);
    LocalDate due2 = created2.toLocalDate().plusDays(1);
    Task t2 = new Task();
    t2.setId(idGenerator.getAndIncrement());
    t2.setTitle("Stub task 2");
    t2.setDescription("Second stub task");
    t2.setCompleted(true);
    t2.setCreatedAt(created2);
    t2.setDueDate(due2);
    t2.setPriority(Priority.HIGH);
    t2.setTags(new HashSet<>(List.of("home", "urgent")));
    stubTasks.put(t2.getId(), t2);
  }

  @Override
  public List<Task> findAll() {
    return new ArrayList<>(stubTasks.values());
  }

  @Override
  public Optional<Task> findById(Long id) {
    return Optional.ofNullable(stubTasks.get(id));
  }

  @Override
  public Task save(Task task) {
    if (task.getId() == null) {
      task.setId(idGenerator.getAndIncrement());
    }
    stubTasks.put(task.getId(), task);
    return task;
  }

  @Override
  public void delete(Long id) {
    stubTasks.remove(id);
  }
}