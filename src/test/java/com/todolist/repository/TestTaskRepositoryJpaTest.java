package com.todolist.repository;

import com.todolist.model.Priority;
import com.todolist.model.Task;
import com.todolist.model.TaskAttachment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TestTaskRepositoryJpaTest {

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TaskAttachmentRepository taskAttachmentRepository;

  private Task newTask(long id, String title, Priority priority, LocalDate dueDate, Set<String> tags) {
    Task t = new Task();
    t.setId(id);
    t.setTitle(title);
    t.setDescription("desc");
    t.setCompleted(false);
    t.setCreatedAt(LocalDateTime.now());
    t.setDueDate(dueDate);
    t.setPriority(priority);
    t.setTags(tags);
    return t;
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

  @Test
  void shouldSaveTaskWithAttachment() {
    LocalDate today = LocalDate.now();
    Task task = new Task();
    task.setTitle("t1");
    task.setDescription("d1");
    task.setCompleted(false);
    task.setCreatedAt(LocalDateTime.now());
    task.setDueDate(today.plusDays(1));
    task.setPriority(Priority.LOW);
    task.setTags(Set.of("tag1"));
    task = taskRepository.save(task);

    TaskAttachment attachment = newAttachment("a.txt", "file-1", task);
    taskAttachmentRepository.save(attachment);

    assertThat(taskAttachmentRepository.findByTask_Id(task.getId())).hasSize(1);
  }

  @Test
  void shouldCascadeRemoveAttachmentsOnTaskDelete() {
    LocalDate today = LocalDate.now();
    Task task = new Task();
    task.setTitle("t1");
    task.setDescription("d1");
    task.setCompleted(false);
    task.setCreatedAt(LocalDateTime.now());
    task.setDueDate(today.plusDays(1));
    task.setPriority(Priority.LOW);
    task.setTags(Set.of("tag1"));
    task = taskRepository.save(task);

    taskAttachmentRepository.save(newAttachment("a.txt", "file-1", task));
    assertThat(taskAttachmentRepository.count()).isEqualTo(1);

    Task reloaded = taskRepository.findById(task.getId()).orElseThrow();
    // Инициализируем коллекцию, чтобы сработал CascadeType.REMOVE по связи.
    reloaded.getAttachments().size();
    taskRepository.delete(reloaded);

    assertThat(taskAttachmentRepository.count()).isEqualTo(0);
  }

  @Test
  void shouldFindDueWithinNext7Days() {
    LocalDate today = LocalDate.now();

    Task dueSoon = new Task();
    dueSoon.setTitle("dueSoon");
    dueSoon.setDescription("d1");
    dueSoon.setCompleted(false);
    dueSoon.setCreatedAt(LocalDateTime.now());
    dueSoon.setDueDate(today.plusDays(3));
    dueSoon.setPriority(Priority.MEDIUM);
    dueSoon.setTags(Set.of("t"));
    dueSoon = taskRepository.save(dueSoon);

    Task dueLater = new Task();
    dueLater.setTitle("dueLater");
    dueLater.setDescription("d2");
    dueLater.setCompleted(false);
    dueLater.setCreatedAt(LocalDateTime.now());
    dueLater.setDueDate(today.plusDays(8));
    dueLater.setPriority(Priority.MEDIUM);
    dueLater.setTags(Set.of("t"));
    dueLater = taskRepository.save(dueLater);

    List<Task> found = taskRepository.findDueWithinNext7Days(today, today.plusDays(7));

    assertThat(found)
        .extracting(Task::getId)
        .containsExactly(dueSoon.getId());
  }

  @Test
  void shouldLoadTasksWithAttachmentsWithoutLazyInitializationIssues() {
    LocalDate today = LocalDate.now();

    Task task1 = new Task();
    task1.setTitle("t1");
    task1.setDescription("d1");
    task1.setCompleted(false);
    task1.setCreatedAt(LocalDateTime.now());
    task1.setDueDate(today.plusDays(1));
    task1.setPriority(Priority.LOW);
    task1.setTags(Set.of("t"));
    task1 = taskRepository.save(task1);

    taskAttachmentRepository.save(newAttachment("a1.txt", "file-1", task1));
    taskAttachmentRepository.save(newAttachment("a2.txt", "file-2", task1));

    assertThat(taskAttachmentRepository.findByTask_Id(task1.getId())).hasSize(2);
  }
}

