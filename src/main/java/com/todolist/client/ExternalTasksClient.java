package com.todolist.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.ExternalTaskCreateRequest;
import com.todolist.dto.ExternalTaskDto;
import com.todolist.exception.ExternalApiException;
import com.todolist.exception.TaskNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ExternalTasksClient {
  private static final Logger log = LoggerFactory.getLogger(ExternalTasksClient.class);
  private static final ParameterizedTypeReference<List<ExternalTaskDto>> TASK_LIST_TYPE = new ParameterizedTypeReference<>() {};

  private final RestClient restClient;
  private final ObjectMapper mapper;

  public ExternalTasksClient(RestClient externalRestClient, ObjectMapper objectMapper) {
    this.restClient = externalRestClient;
    this.mapper = objectMapper;
  }

  @CircuitBreaker(name = "externalApi", fallbackMethod = "createFallback")
  public ExternalTaskDto create(ExternalTaskCreateRequest request) {
    ResponseEntity<ExternalTaskDto> response = restClient.post()
        .uri("/external/v1/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
          throw toClientException(res);
        })
        .toEntity(ExternalTaskDto.class);

    if (response.getStatusCode() != HttpStatus.CREATED) {
      throw new ExternalApiException("Expected 201 Created from external API");
    }

    var location = response.getHeaders().getLocation();
    if (location != null) {
      log.info("External task created, Location={}", location);
    }

    ExternalTaskDto body = response.getBody();
    if (body == null) {
      throw new ExternalApiException("External API returned empty body for create");
    }
    return body;
  }

  @CircuitBreaker(name = "externalApi", fallbackMethod = "getByIdFallback")
  public ExternalTaskDto getById(Long id) {
    return restClient.get()
        .uri("/external/v1/tasks/{id}", id)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
          throw toClientException(res);
        })
        .body(ExternalTaskDto.class);
  }

  @CircuitBreaker(name = "externalApi", fallbackMethod = "listFallback")
  public List<ExternalTaskDto> list(Boolean completed, Integer limit) {
    return restClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/external/v1/tasks")
            .queryParamIfPresent("completed", java.util.Optional.ofNullable(completed))
            .queryParamIfPresent("limit", java.util.Optional.ofNullable(limit))
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
          throw toClientException(res);
        })
        .body(TASK_LIST_TYPE);
  }

  @CircuitBreaker(name = "externalApi", fallbackMethod = "deleteFallback")
  public void delete(Long id) {
    ResponseEntity<Void> response = restClient.delete()
        .uri("/external/v1/tasks/{id}", id)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
          throw toClientException(res);
        })
        .toBodilessEntity();

    if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
      throw new ExternalApiException("Expected 204 No Content from external API");
    }
  }

  private RuntimeException toClientException(org.springframework.http.client.ClientHttpResponse response) {
    try {
      HttpStatus httpStatus = HttpStatus.valueOf(response.getStatusCode().value());
      MediaType contentType = response.getHeaders().getContentType();
      String body =
          response.getBody() != null ? new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8) : "";

      if (httpStatus == HttpStatus.NOT_FOUND) {
        return new TaskNotFoundException(resolveProblemDetailOrDefault(body, "Task not found in external API"));
      }

      if (httpStatus.is5xxServerError()) {
        if (contentType != null && MediaType.TEXT_HTML.includes(contentType)) {
          log.warn("Unexpected HTML 5xx from external API (truncated): {}", safeBody(body));
        }
        return new ExternalApiException("External API returned 5xx (" + httpStatus.value() + ")");
      }

      if (contentType != null && MediaType.TEXT_HTML.includes(contentType)) {
        log.warn("Unexpected content-type {} from external API, body={}", contentType, safeBody(body));
      }

      return new ExternalApiException("External API error: " + httpStatus);
    } catch (ExternalApiException | TaskNotFoundException e) {
      throw e;
    } catch (Exception e) {
      return new ExternalApiException("External API communication error", e);
    }
  }

  private String resolveProblemDetailOrDefault(String body, String defaultMessage) {
    if (body == null || body.isBlank()) {
      return defaultMessage;
    }
    try {
      ProblemDetail pd = mapper.readValue(body, ProblemDetail.class);
      String detail = pd.getDetail();
      return detail != null && !detail.isBlank() ? detail : defaultMessage;
    } catch (Exception ex) {
      return defaultMessage;
    }
  }

  private String safeBody(String body) {
    if (body == null) {
      return "<empty>";
    }
    return body.length() > 300 ? body.substring(0, 300) + "...(truncated)" : body;
  }

  public ExternalTaskDto createFallback(ExternalTaskCreateRequest request, Throwable throwable) {
    ExternalTaskDto dto = new ExternalTaskDto();
    dto.setId(-1L);
    dto.setTitle("fallback-task");
    dto.setDescription("External API temporarily unavailable: " + throwable.getClass().getSimpleName());
    dto.setCompleted(false);
    return dto;
  }

  /** Не смешиваем 404 доменную ошибку с CB-fallback ({@link TaskNotFoundException} не считается failure конфигурацией). */
  public ExternalTaskDto getByIdFallback(Long id, TaskNotFoundException ignored) {
    throw ignored;
  }

  public ExternalTaskDto getByIdFallback(Long id, Throwable throwable) {
    ExternalTaskDto dto = new ExternalTaskDto();
    dto.setId(id);
    dto.setTitle("unavailable");
    dto.setDescription("Fallback response due to external API issue");
    dto.setCompleted(false);
    return dto;
  }

  public List<ExternalTaskDto> listFallback(Boolean completed, Integer limit, Throwable throwable) {
    return List.of();
  }

  public void deleteFallback(Long id, TaskNotFoundException ignored) {
    throw ignored;
  }

  public void deleteFallback(Long id, Throwable throwable) {
    // no-op
  }
}
