package com.todolist.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AccessLogFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    long startedAt = System.currentTimeMillis();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - startedAt;
      String traceId = MDC.get(TraceIdFilter.TRACE_ID);
      log.info(
          "HTTP {} {} -> status={} timeMs={} trace={}",
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          duration,
          traceId
      );
    }
  }
}
