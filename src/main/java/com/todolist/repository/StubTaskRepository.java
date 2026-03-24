package com.todolist.repository;

import com.todolist.dto.Task;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class StubTaskRepository implements TaskRepository {

    private final Map<Long, Task> stubTasks = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(stubTasks.values());
    }

    @Override
    public Optional<Task> getTask(Long id) {
        return Optional.ofNullable(stubTasks.get(id));
    }

    @Override
    public Task insertTask(String description, String title) {
        Task task = Task.builder().title(title).description(description).build();
        task.setId(idGenerator.getAndIncrement());
        stubTasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Task updateTask(Long id, String description, String title, boolean completed) {
        if (id != 0 && stubTasks.containsKey(id)) {
            Task task = Task.builder().description(description).title(title).completed(completed).build();
            stubTasks.put(task.getId(), task);
            return task;
        }
        throw new IllegalArgumentException("Не найдено таски с таким id: " + id);
    }

    @Override
    public void deleteTask(Long id) {
        stubTasks.remove(id);
    }

    @Override
    public void initialize() {
        insertTask("Task 1", "Task description 1");
        insertTask("Task 2", "Task description 2");
    }
}