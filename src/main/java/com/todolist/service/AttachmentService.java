package com.todolist.service;

import com.todolist.model.TaskAttachment;
import com.todolist.repository.TaskAttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AttachmentService {
  private final TaskAttachmentRepository repository;
  private final Path uploadDir = Paths.get("uploads");

  public AttachmentService(TaskAttachmentRepository repository) {
    this.repository = repository;
  }

  public TaskAttachment storeAttachment(Long taskId, MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File must not be empty");
    }

    Files.createDirectories(uploadDir);

    String originalFileName = file.getOriginalFilename();
    String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");
    long size = file.getSize();

    String storedFileName = UUID.randomUUID().toString();
    Path target = uploadDir.resolve(storedFileName);
    Files.copy(file.getInputStream(), target);

    TaskAttachment attachment = new TaskAttachment();
    attachment.setTaskId(taskId);
    attachment.setFileName(originalFileName);
    attachment.setStoredFileName(storedFileName);
    attachment.setContentType(contentType);
    attachment.setSize(size);
    attachment.setUploadedAt(LocalDateTime.now());

    return repository.save(attachment);
  }

  public TaskAttachment getAttachment(Long attachmentId) {
    return repository.findById(attachmentId)
        .orElseThrow(() -> new com.todolist.exception.TaskNotFoundException());
  }

  public Resource loadAsResource(Long attachmentId) {
    TaskAttachment attachment = getAttachment(attachmentId);
    Path filePath = uploadDir.resolve(attachment.getStoredFileName());
    return new FileSystemResource(filePath);
  }

  public List<TaskAttachment> getAttachmentsByTaskId(Long taskId) {
    return repository.findByTaskId(taskId);
  }

  public void deleteAttachment(Long attachmentId) throws IOException {
    TaskAttachment attachment = getAttachment(attachmentId);

    Path filePath = uploadDir.resolve(attachment.getStoredFileName());
    try {
      Files.deleteIfExists(filePath);
    } finally {
      repository.delete(attachmentId);
    }
  }

  public TaskAttachment store(Long taskId, MultipartFile file) throws IOException {
    return storeAttachment(taskId, file);
  }

  public Resource load(Long attachmentId) {
    return loadAsResource(attachmentId);
  }

  public void delete(Long attachmentId) throws IOException {
    deleteAttachment(attachmentId);
  }
}
