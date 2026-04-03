package com.todolist.validation;

import com.todolist.dto.OnUpdate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.util.Arrays;

public class DueDateNotBeforeCreationValidator implements ConstraintValidator<DueDateNotBeforeCreation, LocalDate> {
  private static final ThreadLocal<LocalDate> CREATED_AT_OVERRIDE = new ThreadLocal<>();

  public static void setCreatedAtOverride(LocalDate createdAt) {
    CREATED_AT_OVERRIDE.set(createdAt);
  }

  public static void clearCreatedAtOverride() {
    CREATED_AT_OVERRIDE.remove();
  }

  @Override
  public boolean isValid(LocalDate dueDate, ConstraintValidatorContext context) {
    if (dueDate == null) {
      return true;
    }

    LocalDate override = CREATED_AT_OVERRIDE.get();
    Class<?>[] groups = context.getConstraintDescriptor().getGroups();
    boolean isUpdateGroup = Arrays.stream(groups).anyMatch(g -> g == OnUpdate.class);

    // При OnUpdate валидатору автоматически не доступна createdAt задачи, поэтому "авто-проверку" пропускаем,
    // а реальную проверку выполняем вручную в сервисе, устанавливая ThreadLocal override.
    if (override == null && isUpdateGroup) {
      return true;
    }

    LocalDate createdAtDate = override != null ? override : LocalDate.now();
    return !dueDate.isBefore(createdAtDate);
  }
}

