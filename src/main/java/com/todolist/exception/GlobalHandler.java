package com.todolist.exception;

import com.todolist.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.beans.factory.annotation.Value;

@RestControllerAdvice
public class GlobalHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalHandler.class);
  private final Environment environment;
  @Value("${app.version}")
  private String apiVersion;

  public GlobalHandler(Environment environment) {
    this.environment = environment;
  }

  private boolean isProd() {
    return environment.acceptsProfiles(Profiles.of("prod"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
          MethodArgumentNotValidException ex,
          HttpServletRequest req) {

    Map<String, Object> details = new HashMap<>();

    ex.getBindingResult().getFieldErrors()
            .forEach(e -> details.put(e.getField(), e.getDefaultMessage()));

    ErrorResponse err = new ErrorResponse();
    err.setStatus(400);
    err.setError("Bad Request");
    err.setMessage("Validation failed");
    err.setPath(req.getRequestURI());
    err.setDetails(details);

    return ResponseEntity.badRequest()
        .header("X-API-Version", apiVersion)
        .body(err);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex,
      HttpServletRequest req
  ) {
    Map<String, Object> details = new HashMap<>();
    for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
      String key = String.valueOf(v.getPropertyPath());
      details.put(key, v.getMessage());
    }

    ErrorResponse err = new ErrorResponse();
    err.setStatus(400);
    err.setError("Bad Request");
    err.setMessage("Validation failed");
    err.setPath(req.getRequestURI());
    err.setDetails(details);

    return ResponseEntity.badRequest()
        .header("X-API-Version", apiVersion)
        .body(err);
  }

  @ExceptionHandler(TaskNotFoundException.class)
  public ResponseEntity<ErrorResponse> notFound(HttpServletRequest req) {

    ErrorResponse err = new ErrorResponse();
    err.setStatus(404);
    err.setError("Not Found");
    err.setMessage("Task not found");
    err.setPath(req.getRequestURI());

    return ResponseEntity.status(404)
        .header("X-API-Version", apiVersion)
        .body(err);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> missingParameter(
      MissingServletRequestParameterException ex,
      HttpServletRequest req
  ) {
    ErrorResponse err = new ErrorResponse();
    err.setStatus(400);
    err.setError("Bad Request");
    err.setMessage(ex.getMessage());
    err.setPath(req.getRequestURI());

    return ResponseEntity.badRequest()
        .header("X-API-Version", apiVersion)
        .body(err);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> notReadable(
      HttpMessageNotReadableException ex,
      HttpServletRequest req
  ) {
    ErrorResponse err = new ErrorResponse();
    err.setStatus(400);
    err.setError("Bad Request");
    err.setMessage("Request body is not readable");
    err.setPath(req.getRequestURI());

    return ResponseEntity.badRequest()
        .header("X-API-Version", apiVersion)
        .body(err);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> noHandler(NoHandlerFoundException ex, HttpServletRequest req) {
    ErrorResponse err = new ErrorResponse();
    err.setStatus(404);
    err.setError("Not Found");
    err.setMessage("Endpoint not found");
    err.setPath(req.getRequestURI());

    return ResponseEntity.status(404)
        .header("X-API-Version", apiVersion)
        .body(err);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> all(Exception ex, HttpServletRequest req) {

    ErrorResponse err = new ErrorResponse();
    err.setStatus(500);
    err.setError("Internal Server Error");
    err.setMessage("Unexpected error");
    err.setPath(req.getRequestURI());

    if (isProd()) {
      log.error("Unexpected error in {} (prod, stacktrace suppressed)", req.getRequestURI());
    } else {
      log.error("Unexpected error in {}", req.getRequestURI(), ex);
    }

    return ResponseEntity.status(500)
        .header("X-API-Version", apiVersion)
        .body(err);
  }
}
