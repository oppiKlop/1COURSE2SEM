package com.todolist.service;

import com.todolist.model.TaskAttachment;
import com.todolist.repository.TaskAttachmentRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AttachmentService {
  private final TaskAttachmentRepository repository;
  private final Path root = Paths.get("uploads");

  public AttachmentService(TaskAttachmentRepository repository) throws Exception {
    this.repository = repository;
    Files.createDirectories(root);
  }

  public TaskAttachment store(Long taskId, MultipartFile file) throws Exception {

    String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();

    Path path = root.resolve(storedName);
    Files.copy(file.getInputStream(), path);

    TaskAttachment att = new TaskAttachment();
    att.setTaskId(taskId);
    att.setFileName(file.getOriginalFilename());
    att.setStoredFileName(storedName);
    att.setContentType(file.getContentType());
    att.setSize(file.getSize());
    att.setUploadedAt(LocalDateTime.now());

    return repository.save(att);
  }

  public Resource load(Long id) throws Exception {
    TaskAttachment att = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("File not found"));

    Path path = root.resolve(att.getStoredFileName());

    return new UrlResource(path.toUri());
  }

  public void delete(Long id) throws Exception {
    TaskAttachment att = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("File not found"));

    Files.deleteIfExists(root.resolve(att.getStoredFileName()));
    repository.delete(id);
  }
}
