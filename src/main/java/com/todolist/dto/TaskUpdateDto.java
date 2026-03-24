package com.todolist.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public class TaskUpdateDto {

  @Size(min = 3, max = 100)
  private String title;

  @Size(max = 500)
  private String description;

  private Boolean completed;

  private LocalDate dueDate;

  private Priority priority;

  private Set<String> tags;
}
