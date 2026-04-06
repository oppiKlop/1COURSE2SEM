package com.todolist.service;

import com.todolist.model.Priority;
import com.todolist.model.Task;
import com.todolist.model.TaskAttachment;
import com.todolist.repository.TaskAttachmentRepository;
import com.todolist.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TestTaskServiceWithAttachmentsTest {

  @Autowired
  private TaskService taskService;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TaskAttachmentRepository taskAttachmentRepository;

  @BeforeEach
  void cleanDb() {
    // H2 mem-DB может сохраняться между тест-классами, поэтому данные нужно чистить явно.
    taskAttachmentRepository.deleteAllInBatch();
    taskRepository.deleteAllInBatch();
  }

  @Test
  void allWithAttachments_shouldLoadAttachmentsWithoutNPlusOne() {
    LocalDate today = LocalDate.now();

    Task task1 = new Task();
    task1.setTitle("t1");
    task1.setDescription("d1");
    task1.setCompleted(false);
    task1.setCreatedAt(LocalDateTime.now());
    task1.setDueDate(today.plusDays(1));
    task1.setPriority(Priority.LOW);
    task1.setTags(Set.of("tag1"));
    task1 = taskRepository.save(task1);

    Task task2 = new Task();
    task2.setTitle("t2");
    task2.setDescription("d2");
    task2.setCompleted(false);
    task2.setCreatedAt(LocalDateTime.now());
    task2.setDueDate(today.plusDays(2));
    task2.setPriority(Priority.MEDIUM);
    task2.setTags(Set.of("tag1"));
    task2 = taskRepository.save(task2);

    long id1 = task1.getId();
    long id2 = task2.getId();

    taskAttachmentRepository.save(newAttachment("a1.txt", "file-1", task1));
    taskAttachmentRepository.save(newAttachment("a2.txt", "file-2", task1));
    taskAttachmentRepository.save(newAttachment("b1.txt", "file-3", task2));

    List<Task> tasks = taskService.allWithAttachments();

    assertThat(tasks).hasSize(2);
    assertThat(tasks.stream().filter(t -> t.getId().equals(id1)).findFirst().get().getAttachments())
        .hasSize(2);
    assertThat(tasks.stream().filter(t -> t.getId().equals(id2)).findFirst().get().getAttachments())
        .hasSize(1);
  }

  private TaskAttachment newAttachment(String fileName, String storedFileName, Task task) {
    TaskAttachment att = new TaskAttachment();
    att.setTask(task);
    att.setFileName(fileName);
    att.setStoredFileName(storedFileName);
    att.setContentType("text/plain");
    att.setSize(123L);
    att.setUploadedAt(LocalDateTime.now());
    return att;
  }
}

