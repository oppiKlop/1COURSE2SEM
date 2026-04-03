package com.todolist.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Attachment metadata DTO")
@Data
@NoArgsConstructor
public class AttachmentResponseDto {
  private Long id;
  private String fileName;
  private long size;
  private LocalDateTime uploadedAt;
}
