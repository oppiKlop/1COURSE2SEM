package com.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExternalTaskCreateRequest {
  @NotBlank
  private String title;
  private String description;
  private boolean completed;
}
