package com.todolist.controller;

import com.todolist.dto.TaskResponseDto;
import com.todolist.exception.TaskNotFoundException;
import com.todolist.mapper.TaskMapper;
import com.todolist.model.Priority;
import com.todolist.model.Task;
import com.todolist.security.JwtAuthFilter;
import com.todolist.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = FavoritesController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
    })
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = "app.jpa.auditing.enabled=false")
class TestFavoritesController {

  @MockBean
  private JwtAuthFilter jwtAuthFilter;

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private TaskService taskService;

  @MockBean
  private TaskMapper mapper;

  private Task task(long id) {
    Task t = new Task();
    t.setId(id);
    t.setTitle("t" + id);
    t.setDescription("d" + id);
    t.setCompleted(false);
    t.setCreatedAt(LocalDateTime.now().minusDays(1));
    t.setDueDate(LocalDate.now().plusDays(1));
    t.setPriority(Priority.LOW);
    t.setTags(Set.of("tag"));
    return t;
  }

  @Test
  void addAndDeleteFavorites_shouldUpdateSession() throws Exception {
    mockMvc.perform(post("/api/favorites/{id}", 1L))
        .andExpect(status().isOk())
        .andExpect(header().string("X-API-Version", "2.0.0"));

    mockMvc.perform(delete("/api/favorites/{id}", 1L).sessionAttr("favoriteTaskIds", List.of(1L, 2L)))
        .andExpect(status().isNoContent())
        .andExpect(header().string("X-API-Version", "2.0.0"));
  }

  @Test
  void listFavorites_shouldSkipNotFoundTasks() throws Exception {
    Task t1 = task(1L);
    TaskResponseDto dto1 = new TaskResponseDto();
    dto1.setId(1L);
    dto1.setTitle("t1");

    when(taskService.get(1L)).thenReturn(t1);
    when(taskService.get(2L)).thenThrow(new TaskNotFoundException());
    when(mapper.toDto(t1)).thenReturn(dto1);

    mockMvc.perform(get("/api/favorites").sessionAttr("favoriteTaskIds", List.of(1L, 2L)))
        .andExpect(status().isOk())
        .andExpect(header().string("X-API-Version", "2.0.0"))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(1));

    verify(taskService, times(1)).get(1L);
    verify(taskService, times(1)).get(2L);
    verify(mapper, times(1)).toDto(any(Task.class));
  }
}

