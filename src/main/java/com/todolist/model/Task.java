package com.todolist.model;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
public class Task {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "title", length = 100, nullable = false)
  private String title;

  @Column(name = "description", length = 500)
  private String description;

  @Column(name = "completed", nullable = false)
  private boolean completed;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private java.time.LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "last_modified_at")
  private java.time.LocalDateTime lastModifiedAt;

  @Column(name = "due_date", nullable = false)
  private java.time.LocalDate dueDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "priority", length = 20, nullable = false)
  private Priority priority;

  @Convert(converter = TagsJsonConverter.class)
  @Column(name = "tags", columnDefinition = "TEXT", nullable = false)
  private java.util.Set<String> tags;

  @OneToMany(
      mappedBy = "task",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
      orphanRemoval = true,
      fetch = FetchType.LAZY
  )
  private java.util.List<TaskAttachment> attachments = new java.util.ArrayList<>();
}