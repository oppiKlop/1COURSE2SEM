package com.todolist.model;

import lombok.Builder;
import lombok.Data;

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