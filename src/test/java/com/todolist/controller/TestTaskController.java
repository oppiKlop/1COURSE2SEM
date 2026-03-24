package com.todolist.controller;

import com.todolist.dto.Task;
import com.todolist.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestTaskController {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private TaskService taskService;

    @Test
    void getAllTasksShouldReturnList() {
        Task task1 = Task.builder().id(1L).title("Задача 1").description("Описание 1").build();
        Task task2 = Task.builder().id(2L).title("Задача 2").description("Описание 2").build();
        when(taskService.getAllTasks()).thenReturn(Arrays.asList(task1, task2));

        ResponseEntity<Task[]> response = restTemplate.getForEntity("/api/tasks", Task[].class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(2);
    }

    @Test
    void getAllTasksShouldReturnEmptyList() {
        when(taskService.getAllTasks()).thenReturn(Arrays.asList());

        ResponseEntity<Task[]> response = restTemplate.getForEntity("/api/tasks", Task[].class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }

    @Test
    void getTaskByIdShouldReturnTask() {
        Long id = 1L;
        Task task = Task.builder().id(id).title("Test").description("Description").build();
        when(taskService.getTask(id)).thenReturn(task);

        ResponseEntity<Task> response = restTemplate.getForEntity("/api/tasks/{id}", Task.class, id);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(id);
        assertThat(response.getBody().getTitle()).isEqualTo("Test");
    }

    @Test
    void getTaskByIdShouldReturnError_WhenTaskNotFound() {
        Long id = 999L;
        when(taskService.getTask(id)).thenThrow(new RuntimeException("Задача не найдена"));

        ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/{id}", String.class, id);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    void updateTaskShouldUpdateTask() {
        Long id = 1L;
        Task updatedTask = Task.builder().id(id).title("Обновлено").description("нью описание").build();
        when(taskService.updateTask(anyLong(), anyString(), anyString(), anyBoolean())).thenReturn(updatedTask);

        restTemplate.put("/api/tasks/{id}", updatedTask, id);

        when(taskService.getTask(id)).thenReturn(updatedTask);
        ResponseEntity<Task> response = restTemplate.getForEntity("/api/tasks/{id}", Task.class, id);

        assertThat(response.getBody().getTitle()).isEqualTo("Обновлено");
    }

    @Test
    void updateTaskShouldReturnErrorWhenTaskNotFound() {
        Long id = 999L;
        Task task = Task.builder().id(id).description("fewilanfem").title("velhjvbs").build();
        when(taskService.updateTask(999L, null, null, true)).thenThrow(new RuntimeException("Задача не найдена"));

        restTemplate.put("/api/tasks/{id}", task, id);

        when(taskService.getTask(id)).thenThrow(new RuntimeException("Задача не найдена"));
        ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/{id}", String.class, id);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    void deleteTaskShouldDeleteTask() {
        Long id = 1L;
        doNothing().when(taskService).deleteTask(id);

        restTemplate.delete("/api/tasks/{id}", id);

        verify(taskService, times(1)).deleteTask(id);
    }

    @Test
    void deleteTaskShouldReturnError_WhenTaskNotFound() {
        Long id = 999L;
        doThrow(new RuntimeException("Задача не найдена")).when(taskService).deleteTask(id);

        restTemplate.delete("/api/tasks/{id}", id);

        verify(taskService, times(1)).deleteTask(id);
    }
}