package com.todolist.dto;

import java.time.Instant;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Standard error response")
@Data
@NoArgsConstructor
public class ErrorResponse {
  private Instant timestamp = Instant.now();
  private int status;
  private String error;
  private String message;
  private String path;
  private Map<String, Object> details;
}
