package com.todolist.dto;

import com.todolist.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

public class TaskCreateDto {

  @NotBlank
  @Size(min = 3, max = 100)
  private String title;

  @Size(max = 500)
  private String description;

  @FutureOrPresent
  private LocalDate dueDate;

  @NotNull
  private Priority priority;

  @Size(max = 5)
  private Set<String> tags;
}