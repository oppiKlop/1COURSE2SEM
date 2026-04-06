package com.todolist.service;

import com.todolist.model.Priority;
import com.todolist.model.Task;
import com.todolist.repository.TaskAttachmentRepository;
import com.todolist.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TestTaskStatisticsJdbcServiceTest {

  @Autowired
  private TaskStatisticsJdbcService jdbcService;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TaskAttachmentRepository taskAttachmentRepository;

  @BeforeEach
  void cleanDb() {
    taskAttachmentRepository.deleteAllInBatch();
    taskRepository.deleteAllInBatch();
  }

  @Test
  void getTasksCountByPriority_shouldReturnAggregatedCounts() {
    createTask(Priority.LOW);
    createTask(Priority.LOW);
    createTask(Priority.MEDIUM);

    Map<Priority, Long> stats = jdbcService.getTasksCountByPriority();

    assertThat(stats.get(Priority.LOW)).isEqualTo(2L);
    assertThat(stats.get(Priority.MEDIUM)).isEqualTo(1L);
  }

  private void createTask(Priority priority) {
    Task t = new Task();
    t.setTitle("t");
    t.setDescription("d");
    t.setCompleted(false);
    t.setCreatedAt(LocalDateTime.now());
    t.setDueDate(LocalDate.now().plusDays(1));
    t.setPriority(priority);
    t.setTags(Set.of("tag"));
    taskRepository.save(t);
  }
}

