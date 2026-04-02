package com.todolist.exception;

import com.todolist.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
          MethodArgumentNotValidException ex,
          HttpServletRequest req) {

    Map<String, Object> details = new HashMap<>();

    ex.getBindingResult().getFieldErrors()
            .forEach(e -> details.put(e.getField(), e.getDefaultMessage()));

    ErrorResponse err = new ErrorResponse();
    err.status = 400;
    err.error = "Bad Request";
    err.message = "Validation failed";
    err.path = req.getRequestURI();
    err.details = details;

    return ResponseEntity.badRequest().body(err);
  }

  @ExceptionHandler(TaskNotFoundException.class)
  public ResponseEntity<ErrorResponse> notFound(HttpServletRequest req) {

    ErrorResponse err = new ErrorResponse();
    err.status = 404;
    err.error = "Not Found";
    err.message = "Task not found";
    err.path = req.getRequestURI();

    return ResponseEntity.status(404).body(err);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> all(HttpServletRequest req) {

    ErrorResponse err = new ErrorResponse();
    err.status = 500;
    err.error = "Internal Server Error";
    err.message = "Unexpected error";
    err.path = req.getRequestURI();

    return ResponseEntity.status(500).body(err);
  }
}
