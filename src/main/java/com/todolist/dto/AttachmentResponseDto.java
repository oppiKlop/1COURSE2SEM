package com.todolist.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttachmentResponseDto {
  private Long id;
  private String fileName;
  private long size;
  private LocalDateTime uploadedAt;
}
