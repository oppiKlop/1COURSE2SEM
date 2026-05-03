package com.todolist.config;

import com.todolist.exception.TaskNotFoundException;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

  @Bean
  public CircuitBreakerConfigCustomizer externalApiCircuitBreakerIgnores() {
    return CircuitBreakerConfigCustomizer.of(
        "externalApi",
        builder -> builder.ignoreExceptions(TaskNotFoundException.class));
  }
}
