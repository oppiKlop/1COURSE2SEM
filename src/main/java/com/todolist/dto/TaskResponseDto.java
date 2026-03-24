package com.todolist.dto;

import com.todolist.model.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class TaskResponseDto {
  private Long id;
  private String title;
  private String description;
  private boolean completed;
  private LocalDateTime createdAt;
  private LocalDate dueDate;
  private Priority priority;
  private Set<String> tags;
}