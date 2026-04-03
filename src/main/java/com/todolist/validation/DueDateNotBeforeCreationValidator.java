package com.todolist.validation;

import com.todolist.dto.OnUpdate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

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
    LocalDate createdAtDate = override != null ? override : LocalDate.now();
    return !dueDate.isBefore(createdAtDate);
  }
}

