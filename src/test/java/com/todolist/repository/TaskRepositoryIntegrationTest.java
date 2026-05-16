package com.todolist.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.todolist.model.Priority;
import com.todolist.model.Task;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class TaskRepositoryIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void registerDatasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    registry.add(
        "spring.jpa.properties.hibernate.dialect",
        () -> "org.hibernate.dialect.PostgreSQLDialect");
  }

  @Autowired
  private TaskRepository taskRepository;

  @Test
  void findDueWithinNext7Days_returnsOnlyTasksInDueDateRange() {
    LocalDate today = LocalDate.now();

    Task inWindow = new Task();
    inWindow.setTitle("in-range");
    inWindow.setDescription("d1");
    inWindow.setCompleted(false);
    inWindow.setCreatedAt(LocalDateTime.now());
    inWindow.setDueDate(today.plusDays(3));
    inWindow.setPriority(Priority.MEDIUM);
    inWindow.setTags(Set.of("tc"));
    inWindow = taskRepository.save(inWindow);

    Task outside = new Task();
    outside.setTitle("outside-window");
    outside.setDescription("d2");
    outside.setCompleted(false);
    outside.setCreatedAt(LocalDateTime.now());
    outside.setDueDate(today.plusDays(14));
    outside.setPriority(Priority.MEDIUM);
    outside.setTags(Set.of("tc"));
    taskRepository.save(outside);

    List<Task> found = taskRepository.findDueWithinNext7Days(today, today.plusDays(7));

    assertThat(found).extracting(Task::getId).containsExactly(inWindow.getId());
  }
}

