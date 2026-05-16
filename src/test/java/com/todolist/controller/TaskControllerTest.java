package com.todolist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todolist.exception.GlobalHandler;
import com.todolist.mapper.TaskMapperImpl;
import com.todolist.security.JwtAuthFilter;
import com.todolist.model.Priority;
import com.todolist.model.Task;
import com.todolist.scope.RequestScopedBean;
import com.todolist.service.TaskService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = TaskController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
    })
@AutoConfigureMockMvc(addFilters = false)
@Import({TaskMapperImpl.class, RequestScopedBean.class, GlobalHandler.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.jpa.auditing.enabled=false",
    "app.version=2.0.0",
})
class TaskControllerTest {

  @MockitoBean
  private JwtAuthFilter jwtAuthFilter;

  @MockitoBean
  private TaskService taskService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("POST /api/tasks — valid body — 201 and JSON echoes persisted task fields")
  void postCreatesTask_returns201_andJsonBody() throws Exception {
    LocalDate due = LocalDate.now().plusDays(2);
    String body =
        """
        {
          "title": "Controller slice task",
          "description": "from WebMvcTest",
          "dueDate": "%s",
          "priority": "HIGH",
          "tags": ["test"]
        }
        """
            .formatted(due);

    Task persisted = new Task();
    persisted.setId(501L);
    persisted.setTitle("Controller slice task");
    persisted.setDescription("from WebMvcTest");
    persisted.setCompleted(false);
    persisted.setCreatedAt(LocalDateTime.of(2026, 3, 10, 9, 30));
    persisted.setDueDate(due);
    persisted.setPriority(Priority.HIGH);
    persisted.setTags(Set.of("test"));

    when(taskService.create(any(Task.class))).thenReturn(persisted);

    mockMvc
        .perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(header().string("X-API-Version", "2.0.0"))
        .andExpect(jsonPath("$.id").value(501))
        .andExpect(jsonPath("$.title").value("Controller slice task"))
        .andExpect(jsonPath("$.description").value("from WebMvcTest"))
        .andExpect(jsonPath("$.completed").value(false))
        .andExpect(jsonPath("$.priority").value("HIGH"))
        .andExpect(jsonPath("$.tags[0]").value("test"))
        .andExpect(jsonPath("$.dueDate").value(due.toString()));
  }

  @Test
  @DisplayName("GET /api/tasks/{id} — task exists — 200 and JSON matches stub from given")
  void getTaskById_returns200_andGivenTaskJson() throws Exception {
    long id = 733L;
    Task task = new Task();
    task.setId(id);
    task.setTitle("Loaded task");
    task.setDescription("details");
    task.setCompleted(false);
    task.setCreatedAt(LocalDateTime.of(2026, 4, 1, 12, 0));
    task.setDueDate(LocalDate.now().plusWeeks(1));
    task.setPriority(Priority.LOW);
    task.setTags(Set.of("demo"));

    when(taskService.get(id)).thenReturn(task);

    mockMvc
        .perform(get("/api/tasks/{id}", id))
        .andExpect(status().isOk())
        .andExpect(header().string("X-API-Version", "2.0.0"))
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.title").value("Loaded task"))
        .andExpect(jsonPath("$.description").value("details"))
        .andExpect(jsonPath("$.completed").value(false))
        .andExpect(jsonPath("$.priority").value("LOW"))
        .andExpect(jsonPath("$.tags[0]").value("demo"));
  }

  @Test
  @DisplayName("POST /api/tasks — invalid title length — client error without calling service")
  void postCreatesTask_returns400_whenValidationFails() throws Exception {
    LocalDate due = LocalDate.now().plusDays(1);
    String body =
        """
        {
          "title": "ab",
          "description": "ok",
          "dueDate": "%s",
          "priority": "MEDIUM",
          "tags": []
        }
        """
            .formatted(due);

    mockMvc
        .perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(header().string("X-API-Version", "2.0.0"))
        .andExpect(jsonPath("$.message").value("Validation failed"));
  }
}
