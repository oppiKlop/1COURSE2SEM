package com.todolist.exception;

import java.util.List;

public class TasksBulkCompleteException extends RuntimeException {
  private final List<Long> missingIds;

  public TasksBulkCompleteException(List<Long> missingIds) {
    super("Some tasks were not found: " + missingIds);
    this.missingIds = missingIds;
  }

  public List<Long> getMissingIds() {
    return missingIds;
  }
}

