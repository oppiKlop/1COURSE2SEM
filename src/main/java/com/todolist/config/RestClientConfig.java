package com.todolist.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
  @Bean
  public RestClient externalRestClient(
      RestClient.Builder builder,
      Environment environment,
      @Value("${app.external.base-url}") String fallbackBaseUri,
      @Value("${app.external.connect-timeout-ms:1000}") int connectTimeoutMs,
      @Value("${app.external.read-timeout-ms:2000}") int readTimeoutMs
  ) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(connectTimeoutMs);
    requestFactory.setReadTimeout(readTimeoutMs);

    return builder
        .uriBuilderFactory(new DeferredLocalHostUriBuilderFactory(environment, fallbackBaseUri))
        .requestFactory(requestFactory)
        .defaultHeader(HttpHeaders.USER_AGENT, "Resilient-Secure-Gateway/1.0")
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
