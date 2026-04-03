package com.todolist.controller;

import com.todolist.dto.*;
import com.todolist.exception.TaskNotFoundException;
import com.todolist.model.Priority;
import com.todolist.model.Task;
import com.todolist.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestTaskController {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private TaskService taskService;

    private Task task(long id) {
        Task t = new Task();
        t.setId(id);
        t.setTitle("t" + id);
        t.setDescription("d" + id);
        t.setCompleted(id % 2 == 0);
        t.setCreatedAt(LocalDateTime.now().minusDays(1));
        t.setDueDate(LocalDate.now().plusDays(2));
        t.setPriority(Priority.LOW);
        t.setTags(Set.of("tag"));
        return t;
    }

    @Test
    void getAllTasksShouldReturnList() {
        when(taskService.all()).thenReturn(List.of(task(1), task(2)));

        ResponseEntity<TaskResponseDto[]> response =
            restTemplate.getForEntity("/api/tasks", TaskResponseDto[].class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(2);
        assertThat(response.getHeaders().getFirst("X-Total-Count")).isEqualTo("2");
        assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
    }

    @Test
    void getAllTasksShouldReturn500_WhenServiceThrows() {
        when(taskService.all()).thenThrow(new RuntimeException("boom"));

        ResponseEntity<ErrorResponse> response =
            restTemplate.getForEntity("/api/tasks", ErrorResponse.class);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getTaskByIdShouldReturnTask() {
        long id = 1L;
        when(taskService.get(id)).thenReturn(task(id));

        ResponseEntity<TaskResponseDto> response =
            restTemplate.getForEntity("/api/tasks/{id}", TaskResponseDto.class, id);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(id);
        assertThat(response.getBody().getTitle()).isEqualTo("t" + id);
        assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
    }

    @Test
    void getTaskByIdShouldReturnError_WhenTaskNotFound() {
        long id = 999L;
        when(taskService.get(id)).thenThrow(new TaskNotFoundException());

        ResponseEntity<ErrorResponse> response =
            restTemplate.getForEntity("/api/tasks/{id}", ErrorResponse.class, id);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void updateTaskShouldUpdateTask() {
        long id = 1L;
        Task updated = task(id);
        updated.setTitle("updated");

        TaskUpdateDto dto = new TaskUpdateDto();
        dto.setTitle("updated");
        dto.setDescription("new description");

        when(taskService.update(eq(id), any(TaskUpdateDto.class))).thenReturn(updated);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        ResponseEntity<TaskResponseDto> response =
            restTemplate.exchange(
                "/api/tasks/{id}",
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(dto, headers),
                TaskResponseDto.class,
                id
            );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("updated");
        assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
        verify(taskService, times(1)).update(eq(id), any(TaskUpdateDto.class));
    }

    @Test
    void updateTaskShouldReturn400_WhenValidationFails() {
        long id = 1L;

        TaskUpdateDto dto = new TaskUpdateDto();
        dto.setTitle("ab");

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        ResponseEntity<ErrorResponse> response = restTemplate
            .exchange("/api/tasks/{id}", org.springframework.http.HttpMethod.PUT, new org.springframework.http.HttpEntity<>(dto, headers),
                ErrorResponse.class, id);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
        verify(taskService, never()).update(anyLong(), any(TaskUpdateDto.class));
    }

    @Test
    void createTaskShouldReturn201_WhenValid() {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle("New task");
        dto.setDescription("desc");
        dto.setDueDate(LocalDate.now().plusDays(1));
        dto.setPriority(Priority.MEDIUM);
        dto.setTags(Set.of("tag1"));

        Task created = task(10);
        created.setTitle(dto.getTitle());

        when(taskService.create(any(Task.class))).thenReturn(created);

        ResponseEntity<TaskResponseDto> response =
            restTemplate.postForEntity("/api/tasks", dto, TaskResponseDto.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
    }

    @Test
    void createTaskShouldReturn400_WhenDueDateInPast() {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle("New task");
        dto.setDescription("desc");
        dto.setDueDate(LocalDate.now().minusDays(1));
        dto.setPriority(Priority.MEDIUM);
        dto.setTags(Set.of("tag1"));

        ResponseEntity<ErrorResponse> response =
            restTemplate.postForEntity("/api/tasks", dto, ErrorResponse.class);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
        verify(taskService, never()).create(any(Task.class));
    }

    @Test
    void deleteTaskShouldReturn204_WhenExists() {
        long id = 1L;
        doNothing().when(taskService).delete(id);

        ResponseEntity<Void> response = restTemplate
            .exchange("/api/tasks/{id}", org.springframework.http.HttpMethod.DELETE, null, Void.class, id);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
        verify(taskService, times(1)).delete(id);
    }

    @Test
    void deleteTaskShouldReturn404_WhenTaskNotFound() {
        long id = 999L;
        doThrow(new TaskNotFoundException()).when(taskService).delete(id);

        ResponseEntity<ErrorResponse> response = restTemplate
            .exchange("/api/tasks/{id}", org.springframework.http.HttpMethod.DELETE, null, ErrorResponse.class, id);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
        verify(taskService, times(1)).delete(id);
    }
}