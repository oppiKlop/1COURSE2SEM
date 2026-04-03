package com.todolist.dto;

import com.todolist.model.Priority;
import com.todolist.validation.DueDateNotBeforeCreation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Task creation DTO")
@Data
@NoArgsConstructor
public class TaskCreateDto {
  @NotBlank(groups = OnCreate.class)
  @Size(min = 3, max = 100, groups = OnCreate.class)
  private String title;

  @Size(max = 500, groups = OnCreate.class)
  private String description;

  @FutureOrPresent(groups = OnCreate.class)
  @DueDateNotBeforeCreation(groups = OnCreate.class)
  private LocalDate dueDate;

  @NotNull(groups = OnCreate.class)
  private Priority priority;

  @Size(max = 5, groups = OnCreate.class)
  private Set<String> tags;
}