package com.todolist.repository;

import com.todolist.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Collection;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

  List<TaskAttachment> findByTask_Id(Long taskId);

  List<TaskAttachment> findByTask_IdIn(Collection<Long> taskIds);
}
