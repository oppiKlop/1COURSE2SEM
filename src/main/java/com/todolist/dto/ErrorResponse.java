package com.todolist.dto;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class ErrorResponse {
  public Instant timestamp = Instant.now();
  public int status;
  public String error;
  public String message;
  public String path;
  public Map<String, Object> details;
}
