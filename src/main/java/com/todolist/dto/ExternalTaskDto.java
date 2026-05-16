package com.todolist.dto;

import lombok.Data;

@Data
public class ExternalTaskDto {
  private Long id;
  private String title;
  private String description;
  private boolean completed;
}
