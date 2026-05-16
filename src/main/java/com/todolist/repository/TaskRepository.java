package com.todolist.repository;

import com.todolist.model.Task;
import com.todolist.model.Priority;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

  List<Task> findByCompletedAndPriority(boolean completed, Priority priority);

  @Query("select t from Task t where t.dueDate between :start and :end")
  List<Task> findDueWithinNext7Days(
      @Param("start") java.time.LocalDate start,
      @Param("end") java.time.LocalDate end
  );

  @EntityGraph(attributePaths = "attachments")
  @Query("select t from Task t")
  List<Task> findAllWithAttachments();

  @EntityGraph(attributePaths = "attachments")
  @Query("select t from Task t where t.id = :id")
  Optional<Task> findByIdWithAttachments(@Param("id") Long id);
}
