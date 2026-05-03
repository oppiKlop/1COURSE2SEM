package com.todolist.dto;

import com.todolist.model.Priority;
import jakarta.validation.constraints.Size;
import com.todolist.validation.DueDateNotBeforeCreation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Task update DTO (partial update)")
@Data
@NoArgsConstructor
public class TaskUpdateDto {
  @Size(min = 3, max = 100, groups = OnUpdate.class)
  private String title;
  @Size(max = 500, groups = OnUpdate.class)
  private String description;
  private Boolean completed;
  @DueDateNotBeforeCreation(groups = OnUpdate.class)
  private LocalDate dueDate;
  private Priority priority;
  @Size(max = 5, groups = OnUpdate.class)
  private Set<String> tags;
}
