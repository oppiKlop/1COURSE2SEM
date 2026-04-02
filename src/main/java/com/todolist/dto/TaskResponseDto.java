package com.todolist.dto;

import com.todolist.model.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class TaskResponseDto {
  public Long id;
  public String title;
  public String description;
  public boolean completed;
  public LocalDateTime createdAt;
  public LocalDate dueDate;
  public Priority priority;
  public Set<String> tags;
}