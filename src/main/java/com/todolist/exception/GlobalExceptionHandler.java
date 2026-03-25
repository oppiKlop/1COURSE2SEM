package com.todolist.exception;

import com.todolist.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(TaskNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
          TaskNotFoundException ex,
          HttpServletRequest request
  ) {
    return buildError(ex, request, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAll(
          Exception ex,
          HttpServletRequest request
  ) {
    return buildError(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<ErrorResponse> buildError(
          Exception ex,
          HttpServletRequest request,
          HttpStatus status
  ) {
    ErrorResponse err = new ErrorResponse();
    err.setTimestamp(Instant.now());
    err.setStatus(status.value());
    err.setError(status.getReasonPhrase());
    err.setMessage(ex.getMessage());
    err.setPath(request.getRequestURI());
    err.setDetails(new HashMap<>());

    return ResponseEntity.status(status).body(err);
  }
}
