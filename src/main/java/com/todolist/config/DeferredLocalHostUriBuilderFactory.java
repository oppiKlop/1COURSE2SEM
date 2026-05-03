package com.todolist.config;

import java.net.URI;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

/**
 * Uses {@code local.server.port} when present (integration tests with random port); otherwise {@code
 * fallbackBaseUri} from config (normally {@code http://127.0.0.1:${server.port}} for same-process
 * external emulator).
 */
public final class DeferredLocalHostUriBuilderFactory implements UriBuilderFactory {

  private final Environment environment;
  private final String fallbackBaseUri;

  public DeferredLocalHostUriBuilderFactory(Environment environment, String fallbackBaseUri) {
    this.environment = environment;
    this.fallbackBaseUri = fallbackBaseUri;
  }

  private UriBuilderFactory delegate() {
    String baseUri = resolveBaseUri();
    return new DefaultUriBuilderFactory(baseUri);
  }

  private String resolveBaseUri() {
    Integer localPort = environment.getProperty("local.server.port", Integer.class);
    if (localPort != null && localPort > 0) {
      return "http://127.0.0.1:" + localPort;
    }
    return fallbackBaseUri;
  }

  @Override
  public UriBuilder uriString(String uriTemplate) {
    return delegate().uriString(uriTemplate);
  }

  @Override
  public UriBuilder builder() {
    return delegate().builder();
  }

  @Override
  public URI expand(String uriTemplate, Map<String, ?> uriVariables) {
    return delegate().expand(uriTemplate, uriVariables);
  }

  @Override
  public URI expand(String uriTemplate, Object... uriVariableValues) {
    return delegate().expand(uriTemplate, uriVariableValues);
  }
}
