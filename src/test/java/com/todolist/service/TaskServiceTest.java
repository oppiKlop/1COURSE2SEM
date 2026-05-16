package com.todolist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.todolist.dto.TaskUpdateDto;
import com.todolist.model.Priority;
import com.todolist.model.Task;
import com.todolist.repository.TaskRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = "app.jpa.auditing.enabled=false")
class TaskServiceTest {

  @MockitoBean
  private TaskRepository taskRepository;

  @Autowired
  private TaskService taskService;

  @Test
  @DisplayName("given persisted task — when completing via update — then repository save receives completed=true")
  void updateStatus_existingTaskMarksCompleted_verifyRepositoryInteractions() {
    // given
    long id = 42L;
    Task existing = new Task();
    existing.setId(id);
    existing.setTitle("wash dog");
    existing.setDescription("soon");
    existing.setCompleted(false);
    existing.setCreatedAt(LocalDateTime.of(2026, 1, 15, 10, 0));
    existing.setDueDate(LocalDate.of(2026, 2, 1));
    existing.setPriority(Priority.MEDIUM);
    existing.setTags(Set.of("home"));

    when(taskRepository.findById(id)).thenReturn(Optional.of(existing));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    TaskUpdateDto dto = new TaskUpdateDto();
    dto.setCompleted(true);

    // when
    Task result = taskService.update(id, dto);

    // then
    assertThat(result.isCompleted()).isTrue();

    verify(taskRepository, times(1)).findById(id);
    ArgumentCaptor<Task> savedCaptor = ArgumentCaptor.forClass(Task.class);
    verify(taskRepository).save(savedCaptor.capture());
    assertThat(savedCaptor.getValue().getId()).isEqualTo(id);
    assertThat(savedCaptor.getValue().isCompleted()).isTrue();
  }
}
