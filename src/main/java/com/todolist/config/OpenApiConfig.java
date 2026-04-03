package com.todolist.config;

import io.swagger.v3.oas.models.Contact;
import io.swagger.v3.oas.models.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI(
      @Value("${app.name}") String appName,
      @Value("${app.version}") String version
  ) {
    Contact contact = new Contact()
        .name("To-Do List Team")
        .email("support@example.com");

    Info info = new Info()
        .title(appName)
        .version(version)
        .description("To-Do List API (MVP на Spring Boot)")
        .contact(contact);

    return new OpenAPI().info(info);
  }
}

