package com.todolist.controller;

import com.todolist.dto.AttachmentResponseDto;
import com.todolist.model.TaskAttachment;
import com.todolist.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class AttachmentController {

  private final AttachmentService service;

  @PostMapping("/api/tasks/{taskId}/attachments")
  public ResponseEntity<AttachmentResponseDto> upload(
          @PathVariable Long taskId,
          @RequestParam("file") MultipartFile file) throws Exception {

    TaskAttachment att = service.store(taskId, file);

    AttachmentResponseDto dto = new AttachmentResponseDto();
    dto.setId(att.getId());
    dto.setFileName(att.getFileName());
    dto.setSize(att.getSize());
    dto.setUploadedAt(att.getUploadedAt());

    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @GetMapping("/api/attachments/{id}")
  public ResponseEntity<Resource> download(@PathVariable Long id) throws Exception {

    Resource resource = service.load(id);

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
  }

  @DeleteMapping("/api/attachments/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}