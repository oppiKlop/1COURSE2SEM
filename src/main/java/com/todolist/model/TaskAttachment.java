package com.todolist.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskAttachment {
  private Long id;
  private Long taskId;
  private String fileName;
  private String storedFileName;
  private String contentType;
  private long size;
  private LocalDateTime uploadedAt;
}