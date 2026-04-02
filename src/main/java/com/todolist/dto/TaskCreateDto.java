package com.todolist.dto;

import com.todolist.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

public class TaskCreateDto {
  @NotBlank(groups = OnCreate.class)
  @Size(min = 3, max = 100, groups = OnCreate.class)
  public String title;

  @Size(max = 500)
  public String description;

  @FutureOrPresent
  public LocalDate dueDate;

  @NotNull(groups = OnCreate.class)
  public Priority priority;

  @Size(max = 5)
  public Set<String> tags;
}