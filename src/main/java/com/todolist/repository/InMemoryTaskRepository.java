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
    private final Map<Long, Task> map = new ConcurrentHashMap<>();
    private final AtomicLong id = new AtomicLong();

    public List<Task> findAll() {
        return new ArrayList<>(map.values());
    }

    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(map.get(id));
    }

    public Task save(Task t) {
        if (t.getId() == null) t.setId(id.incrementAndGet());
        map.put(t.getId(), t);
        return t;
    }

    public void delete(Long id) {
        map.remove(id);
    }
}