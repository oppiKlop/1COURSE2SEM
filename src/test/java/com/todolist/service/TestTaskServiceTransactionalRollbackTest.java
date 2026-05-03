package com.todolist.service;

import com.todolist.exception.TaskNotFoundException;
import com.todolist.exception.TasksBulkCompleteException;
import com.todolist.model.Priority;
import com.todolist.model.Task;
import com.todolist.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class TestTaskServiceTransactionalRollbackTest {

  @Autowired
  private TaskService taskService;

  @Autowired
  private TaskRepository taskRepository;

  @Test
  void bulkCompleteTasks_shouldRollbackWhenAnyIdMissing() {
    Task t1 = new Task();
    t1.setTitle("t1");
    t1.setDescription("d1");
    t1.setCompleted(false);
    t1.setCreatedAt(LocalDateTime.now());
    t1.setDueDate(LocalDate.now().plusDays(1));
    t1.setPriority(Priority.LOW);
    t1.setTags(Set.of("tag1"));
    t1 = taskRepository.save(t1);

    Task t2 = new Task();
    t2.setTitle("t2");
    t2.setDescription("d2");
    t2.setCompleted(false);
    t2.setCreatedAt(LocalDateTime.now());
    t2.setDueDate(LocalDate.now().plusDays(1));
    t2.setPriority(Priority.LOW);
    t2.setTags(Set.of("tag1"));
    t2 = taskRepository.save(t2);

    long missingId = 999999L;

    long id1 = t1.getId();
    long id2 = t2.getId();

    assertThatThrownBy(() -> taskService.bulkCompleteTasks(List.of(id1, missingId, id2)))
        .isInstanceOf(TasksBulkCompleteException.class);

    Task t1Reloaded = taskRepository.findById(id1).orElseThrow(TaskNotFoundException::new);
    Task t2Reloaded = taskRepository.findById(id2).orElseThrow(TaskNotFoundException::new);

    assertThat(t1Reloaded.isCompleted()).isFalse();
    assertThat(t2Reloaded.isCompleted()).isFalse();
  }
}

