package com.todolist.repository;

import com.todolist.model.Task;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Primary
public class InMemoryTaskRepository implements TaskRepository {
    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Optional<Task> getTask(Long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public Task insertTask(String description, String title) {
        Task task = Task.builder().description(description).title(title).build();
        task.setId(idGenerator.getAndIncrement());
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Task updateTask(Long id, String description, String title, boolean completed) {
        if (id != 0 && tasks.containsKey(id)) {
            Task task = Task.builder().title(title).completed(completed).description(description).build();
            tasks.put(id, task);
            return task;
        }
        throw new IllegalArgumentException("Не найдено таски с таким id: " + id);
    }

    @Override
    public void deleteTask(Long id) {
        tasks.remove(id);
    }

    @Override
    public void initialize() {}
}
