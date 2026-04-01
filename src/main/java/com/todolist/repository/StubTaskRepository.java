package com.todolist.repository;

import com.todolist.model.Task;
import com.todolist.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    public Task save(Task task) {
        if (task.getId() == null) {
            task.setId(idGenerator.getAndIncrement());
        }
        stubTasks.put(task.getId(), task);
        return task;
    }

    @Override
    public void deleteTask(Long id) {
        stubTasks.remove(id);
    }
}