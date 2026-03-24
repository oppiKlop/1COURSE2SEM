package com.todolist.repository;


import com.todolist.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    List<Task> getAllTasks();

    Optional<Task> getTask(Long id);

    Task save(Task task);

    void deleteTask(Long id);
}
