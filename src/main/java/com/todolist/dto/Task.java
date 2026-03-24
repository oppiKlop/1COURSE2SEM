package com.todolist.dto;

import jakarta.annotation.Priority;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Builder
@Data
public class Task {
    private Long id;
    private String title;
    private String description;
    private boolean completed = false;
    private LocalDateTime createdAt;
    private LocalDate dueDate;
    private Priority priority;
    private Set<String> tags;
}