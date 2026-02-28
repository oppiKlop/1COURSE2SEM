package com.todolist.repository;


import com.todolist.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    void initialize();

    Task insertTask(String description, String title);

    Optional<Task> getTask(Long id);

    List<Task> getAllTasks();

    Task updateTask(Long id, String description, String title, boolean completed);

    void deleteTask(Long id);
}
