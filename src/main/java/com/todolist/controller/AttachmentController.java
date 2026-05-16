package com.todolist.controller;

import com.todolist.dto.AttachmentResponseDto;
import com.todolist.model.TaskAttachment;
import com.todolist.service.AttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;

@RestController
public class AttachmentController {

  private final AttachmentService service;
  private final String apiVersion;

  public AttachmentController(AttachmentService service, @Value("${app.version}") String apiVersion) {
    this.service = service;
    this.apiVersion = apiVersion;
  }

  @PostMapping("/api/tasks/{taskId}/attachments")
  @Operation(summary = "Upload attachment for a task")
  @ApiResponse(responseCode = "201", description = "Attachment created")
  public ResponseEntity<AttachmentResponseDto> upload(
          @PathVariable Long taskId,
          @RequestParam("file") MultipartFile file) throws Exception {

    TaskAttachment att = service.storeAttachment(taskId, file);

    AttachmentResponseDto dto = new AttachmentResponseDto();
    dto.setId(att.getId());
    dto.setFileName(att.getFileName());
    dto.setSize(att.getSize());
    dto.setUploadedAt(att.getUploadedAt());

    return ResponseEntity.status(HttpStatus.CREATED)
        .header("X-API-Version", apiVersion)
        .body(dto);
  }

  @GetMapping("/api/tasks/{taskId}/attachments")
  @Operation(summary = "List attachments for a task")
  @ApiResponse(responseCode = "200", description = "Attachments returned")
  public ResponseEntity<List<AttachmentResponseDto>> list(@PathVariable Long taskId) {
    List<AttachmentResponseDto> result = service.getAttachmentsByTaskId(taskId).stream().map(att -> {
      AttachmentResponseDto dto = new AttachmentResponseDto();
      dto.setId(att.getId());
      dto.setFileName(att.getFileName());
      dto.setSize(att.getSize());
      dto.setUploadedAt(att.getUploadedAt());
      return dto;
    }).toList();

    return ResponseEntity.ok()
        .header("X-API-Version", apiVersion)
        .body(result);
  }

  @GetMapping("/api/attachments/{attachmentId}")
  @Operation(summary = "Download attachment by id")
  @ApiResponse(responseCode = "200", description = "File downloaded")
  public ResponseEntity<Resource> download(@PathVariable Long attachmentId) {

    TaskAttachment attachment = service.getAttachment(attachmentId);
    Resource resource = service.loadAsResource(attachmentId);

    return ResponseEntity.ok()
            .header("X-API-Version", apiVersion)
            .contentType(attachment.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(attachment.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + attachment.getFileName() + "\"")
            .body(resource);
  }

  @DeleteMapping("/api/attachments/{attachmentId}")
  @Operation(summary = "Delete attachment by id")
  @ApiResponse(responseCode = "204", description = "Attachment deleted")
  public ResponseEntity<Void> delete(@PathVariable Long attachmentId) throws Exception {
    service.deleteAttachment(attachmentId);
    return ResponseEntity.noContent()
        .header("X-API-Version", apiVersion)
        .build();
  }
}