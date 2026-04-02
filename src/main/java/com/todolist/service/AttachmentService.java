package com.todolist.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class AttachmentService {
  public void save(Long taskId, MultipartFile file) throws IOException {
    String name = UUID.randomUUID().toString();
    Path path = Paths.get("uploads/" + name);
    Files.copy(file.getInputStream(), path);
  }
}
