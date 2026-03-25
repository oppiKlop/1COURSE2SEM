package com.todolist.repository;

import com.todolist.model.TaskAttachment;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryTaskAttachmentRepository implements TaskAttachmentRepository {
  private final Map<Long, TaskAttachment> storage = new ConcurrentHashMap<>();
  private final AtomicLong idGen = new AtomicLong(1);

  @Override
  public TaskAttachment save(TaskAttachment att) {
    if (att.getId() == null) {
      att.setId(idGen.getAndIncrement());
    }
    storage.put(att.getId(), att);
    return att;
  }

  @Override
  public Optional<TaskAttachment> findById(Long id) {
    return Optional.ofNullable(storage.get(id));
  }

  @Override
  public List<TaskAttachment> findByTaskId(Long taskId) {
    return storage.values().stream()
            .filter(a -> a.getTaskId().equals(taskId))
            .toList();
  }

  @Override
  public void delete(Long id) {
    storage.remove(id);
  }
}
