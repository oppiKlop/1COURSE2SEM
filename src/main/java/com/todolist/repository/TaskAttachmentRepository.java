package com.todolist.repository;

import com.todolist.model.TaskAttachment;

import java.util.*;

public interface TaskAttachmentRepository {

  TaskAttachment save(TaskAttachment attachment);

  Optional<TaskAttachment> findById(Long id);

  List<TaskAttachment> findByTaskId(Long taskId);

  void delete(Long id);
}
