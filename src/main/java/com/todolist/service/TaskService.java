package com.todolist.service;

import com.todolist.dto.OnUpdate;
import com.todolist.dto.TaskUpdateDto;
import com.todolist.exception.TaskNotFoundException;
import com.todolist.exception.TasksBulkCompleteException;
import com.todolist.mapper.TaskMapper;
import com.todolist.model.Task;
import com.todolist.model.TaskAttachment;
import com.todolist.repository.TaskRepository;
import com.todolist.repository.TaskAttachmentRepository;
import com.todolist.validation.DueDateNotBeforeCreationValidator;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskService {
  private static final Logger log = LoggerFactory.getLogger(TaskService.class);

  private final TaskRepository repo;
  private final TaskAttachmentRepository attachmentRepository;
  private final TaskMapper mapper;
  private final Validator validator;

  public TaskService(
      TaskRepository repo,
      TaskAttachmentRepository attachmentRepository,
      TaskMapper mapper,
      Validator validator
  ) {
    this.repo = repo;
    this.attachmentRepository = attachmentRepository;
    this.mapper = mapper;
    this.validator = validator;
  }

  @PostConstruct
  void init() {
    log.info("TaskService initialized");
  }

  public List<Task> all() {
    return repo.findAll();
  }

  public Task get(Long id) {
    return repo.findById(id)
        .orElseThrow(TaskNotFoundException::new);
  }

  public Task create(Task t) {
    if (t.getTags() == null) {
      t.setTags(Set.of());
    }
    return repo.save(t);
  }

  public Task update(Long id, TaskUpdateDto dto) {
    Task task = get(id);

    if (dto.getDueDate() != null) {
      LocalDate createdAtDate = task.getCreatedAt().toLocalDate();
      DueDateNotBeforeCreationValidator.setCreatedAtOverride(createdAtDate);
      try {
        Set<ConstraintViolation<TaskUpdateDto>> violations = validator.validate(dto, OnUpdate.class);
        if (!violations.isEmpty()) {
          throw new ConstraintViolationException(violations);
        }
      } finally {
        DueDateNotBeforeCreationValidator.clearCreatedAtOverride();
      }
    }

    mapper.update(dto, task);
    return repo.save(task);
  }

  @org.springframework.transaction.annotation.Transactional
  public void delete(Long id) {
    Task task = repo.findByIdWithAttachments(id)
        .orElseThrow(TaskNotFoundException::new);

    if (task.getAttachments() != null) {
      task.getAttachments().size();
    }

    repo.delete(task);
  }

  @org.springframework.transaction.annotation.Transactional
  public void bulkCompleteTasks(List<Long> ids) {
    Set<Long> uniqueIds = Set.copyOf(ids);
    List<Task> tasks = repo.findAllById(uniqueIds);

    Set<Long> foundIds = tasks.stream()
        .map(Task::getId)
        .collect(java.util.stream.Collectors.toSet());

    List<Long> missing = uniqueIds.stream()
        .filter(id -> !foundIds.contains(id))
        .toList();

    for (Task task : tasks) {
      task.setCompleted(true);
    }
    repo.saveAll(tasks);

    if (!missing.isEmpty()) {
      throw new TasksBulkCompleteException(missing);
    }
  }

  public List<Task> allWithAttachments() {
    List<Task> tasks = repo.findAll();
    if (tasks.isEmpty()) {
      return List.of();
    }

    List<Long> taskIds = tasks.stream()
        .map(Task::getId)
        .toList();

    List<TaskAttachment> attachments = attachmentRepository.findByTask_IdIn(taskIds);

    Map<Long, List<TaskAttachment>> byTaskId = attachments.stream()
        .collect(Collectors.groupingBy(TaskAttachment::getTaskId));

    for (Task task : tasks) {
      List<TaskAttachment> taskAttachments = byTaskId.getOrDefault(task.getId(), List.of());
      task.setAttachments(new java.util.ArrayList<>(taskAttachments));
    }

    return tasks;
  }

  public List<Task> dueWithinNext7Days() {
    LocalDate today = LocalDate.now();
    return repo.findDueWithinNext7Days(today, today.plusDays(7));
  }
}