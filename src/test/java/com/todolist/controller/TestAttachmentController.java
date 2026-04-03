package com.todolist.controller;

import com.todolist.exception.GlobalHandler;
import com.todolist.exception.TaskNotFoundException;
import com.todolist.model.TaskAttachment;
import com.todolist.service.AttachmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = AttachmentController.class)
@Import(GlobalHandler.class)
class TestAttachmentController {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AttachmentService service;

  @Test
  void uploadAndListAndDelete_shouldWork() throws Exception {
    long taskId = 1L;
    MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", "hello".getBytes());

    TaskAttachment att = new TaskAttachment();
    att.setId(10L);
    att.setTaskId(taskId);
    att.setFileName("hello.txt");
    att.setStoredFileName("uuid");
    att.setContentType("text/plain");
    att.setSize(5L);
    att.setUploadedAt(LocalDateTime.now());

    when(service.storeAttachment(eq(taskId), any())).thenReturn(att);
    when(service.getAttachmentsByTaskId(taskId)).thenReturn(List.of(att));

    mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", taskId).file(file))
        .andExpect(status().isCreated())
        .andExpect(header().string("X-API-Version", "2.0.0"))
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.fileName").value("hello.txt"));

    mockMvc.perform(get("/api/tasks/{taskId}/attachments", taskId))
        .andExpect(status().isOk())
        .andExpect(header().string("X-API-Version", "2.0.0"))
        .andExpect(jsonPath("$", hasSize(1)));

    doNothing().when(service).deleteAttachment(10L);
    mockMvc.perform(delete("/api/attachments/{attachmentId}", 10L))
        .andExpect(status().isNoContent())
        .andExpect(header().string("X-API-Version", "2.0.0"));
  }

  @Test
  void download_shouldSetContentDisposition() throws Exception {
    long attachmentId = 10L;

    TaskAttachment att = new TaskAttachment();
    att.setId(attachmentId);
    att.setFileName("hello.txt");
    att.setStoredFileName("uuid");
    att.setContentType("text/plain");

    when(service.getAttachment(attachmentId)).thenReturn(att);
    when(service.loadAsResource(attachmentId)).thenReturn(new ByteArrayResource("hello".getBytes()) {
      @Override
      public String getFilename() {
        return "ignored";
      }
    });

    mockMvc.perform(get("/api/attachments/{attachmentId}", attachmentId))
        .andExpect(status().isOk())
        .andExpect(header().string("X-API-Version", "2.0.0"))
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"hello.txt\""));
  }

  @Test
  void download_shouldReturn404_WhenNotFound() throws Exception {
    long attachmentId = 999L;
    when(service.getAttachment(attachmentId)).thenThrow(new TaskNotFoundException());

    mockMvc.perform(get("/api/attachments/{attachmentId}", attachmentId))
        .andExpect(status().isNotFound())
        .andExpect(header().string("X-API-Version", "2.0.0"))
        .andExpect(jsonPath("$.status").value(404));
  }
}

