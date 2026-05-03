package com.todolist.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.todolist.dto.ExternalTaskCreateRequest;
import com.todolist.dto.ExternalTaskDto;
import com.todolist.dto.LoginRequest;
import com.todolist.dto.LoginResponse;
import com.todolist.service.TasksGatewayService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = "app.jpa.auditing.enabled=false")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GatewaySecurityAndTasksIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private TasksGatewayService tasksGatewayService;

  @Autowired
  private CircuitBreakerRegistry circuitBreakerRegistry;

  @BeforeEach
  void resetCircuitBreakerBetweenTests() {
    circuitBreakerRegistry.circuitBreaker("externalApi").reset();
  }

  private String loginAccessToken(String username) {
    LoginRequest login = new LoginRequest();
    login.setUsername(username);
    login.setPassword("password");

    ResponseEntity<LoginResponse> resp = restTemplate.postForEntity(
        "/api/v1/auth/login",
        new HttpEntity<>(login, jsonHeadersWithoutAuth()),
        LoginResponse.class
    );
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(resp.getBody()).isNotNull();
    assertThat(resp.getBody().getAccessToken()).isNotBlank();
    return Objects.requireNonNull(resp.getBody()).getAccessToken();
  }

  private HttpHeaders jsonHeadersWithoutAuth() {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    h.setAccept(List.of(MediaType.APPLICATION_JSON));
    return h;
  }

  private HttpHeaders bearerHeaders(String token) {
    HttpHeaders h = jsonHeadersWithoutAuth();
    h.setBearerAuth(token);
    return h;
  }

  @Test
  @Order(5)
  void login_returnsJwt() {
    loginAccessToken("user");
  }

  @Test
  @Order(10)
  void profile_withoutToken_returns401Json() {
    ResponseEntity<String> resp = restTemplate.exchange(
        "/api/v1/profile",
        HttpMethod.GET,
        new HttpEntity<>(jsonHeadersWithoutAuth()),
        String.class
    );
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(resp.getBody()).contains("401");
  }

  @Test
  @Order(15)
  void profile_withUserToken_returns200() {
    ResponseEntity<String> resp = restTemplate.exchange(
        "/api/v1/profile",
        HttpMethod.GET,
        new HttpEntity<>(bearerHeaders(loginAccessToken("user"))),
        String.class
    );
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(resp.getBody()).contains("user");
  }

  @Test
  @Order(16)
  void docs_withUserToken_returns403Json() {
    ResponseEntity<String> resp = restTemplate.exchange(
        "/api/v1/docs",
        HttpMethod.GET,
        new HttpEntity<>(bearerHeaders(loginAccessToken("user"))),
        String.class
    );
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(resp.getBody()).contains("403");
  }

  @Test
  @Order(17)
  void docs_withReaderToken_returns200() {
    ResponseEntity<String> resp = restTemplate.exchange(
        "/api/v1/docs",
        HttpMethod.GET,
        new HttpEntity<>(bearerHeaders(loginAccessToken("reader"))),
        String.class
    );
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  @Order(20)
  void tasksGateway_crudThroughExternalEmulator_worksWithJwt() {
    String token = loginAccessToken("user");

    ExternalTaskCreateRequest req = new ExternalTaskCreateRequest();
    req.setTitle("Gateway task");
    req.setDescription("via RestClient emulator");
    req.setCompleted(false);

    ResponseEntity<ExternalTaskDto> createdResp = restTemplate.exchange(
        "/api/v1/tasks",
        HttpMethod.POST,
        new HttpEntity<>(req, bearerHeaders(token)),
        ExternalTaskDto.class
    );
    assertThat(createdResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(createdResp.getHeaders().getLocation()).isNotNull();
    assertThat(createdResp.getBody()).isNotNull();
    long id = Objects.requireNonNull(createdResp.getBody()).getId();

    ResponseEntity<ExternalTaskDto> getResp = restTemplate.exchange(
        "/api/v1/tasks/" + id,
        HttpMethod.GET,
        new HttpEntity<>(bearerHeaders(token)),
        ExternalTaskDto.class
    );
    assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(Objects.requireNonNull(getResp.getBody()).getId()).isEqualTo(id);

    ResponseEntity<List<ExternalTaskDto>> listResp = restTemplate.exchange(
        "/api/v1/tasks?completed=false&limit=10",
        HttpMethod.GET,
        new HttpEntity<>(bearerHeaders(token)),
        new ParameterizedTypeReference<List<ExternalTaskDto>>() {}
    );
    assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listResp.getBody()).isNotNull();

    ResponseEntity<Void> deleteResp = restTemplate.exchange(
        "/api/v1/tasks/" + id,
        HttpMethod.DELETE,
        new HttpEntity<>(bearerHeaders(token)),
        Void.class
    );
    assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  @Order(25)
  void tasksGateway_missingExternalTask_returns404WithMeaningfulMessage() {
    String token = loginAccessToken("user");
    ResponseEntity<String> resp = restTemplate.exchange(
        "/api/v1/tasks/999999",
        HttpMethod.GET,
        new HttpEntity<>(bearerHeaders(token)),
        String.class
    );
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(resp.getBody()).contains("404");
    assertThat(resp.getBody()).isNotBlank();
  }

  /** Проверяет срабатывание {@code @RateLimiter} на прокси бина сервиса (AOP после снятого circuit breaker). */
  @Test
  @Order(35)
  void rateLimiter_triggersViaGatewayServiceFacade() throws Exception {
    Thread.sleep(1200); // освежить окно Ratelimiter (limitForPeriod=5 / 1s) после других gateway-тестов
    String token = loginAccessToken("user");
    ExternalTaskCreateRequest req = new ExternalTaskCreateRequest();
    req.setTitle("RL check");
    req.setDescription("");
    req.setCompleted(false);
    long id =
        Objects.requireNonNull(restTemplate.exchange(
                    "/api/v1/tasks",
                    HttpMethod.POST,
                    new HttpEntity<>(req, bearerHeaders(token)),
                    ExternalTaskDto.class)
                .getBody())
            .getId();

    RequestNotPermitted denial = null;
    for (int i = 0; i < 50; i++) {
      try {
        tasksGatewayService.getTask(id);
      } catch (Exception e) {
        Throwable c = e;
        while (c != null) {
          if (c instanceof RequestNotPermitted r) {
            denial = r;
            break;
          }
          c = c.getCause();
        }
        if (denial != null) {
          break;
        }
      }
      Thread.sleep(1);
    }
    assertThat(denial).isNotNull();
  }
}
