package com.todolist.repository;


import com.todolist.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    List<Task> findAll();

    Optional<Task> findById(Long id);

    Task save(Task task);

    void delete(Long id);
}
