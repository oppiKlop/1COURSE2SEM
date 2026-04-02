package com.todolist.dto;

import com.todolist.model.Priority;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public class TaskUpdateDto {
  @Size(min = 3, groups = OnUpdate.class)
  public String title;
  public String description;
  public Boolean completed;
  public LocalDate dueDate;
  public Priority priority;
  public Set<String> tags;
}
