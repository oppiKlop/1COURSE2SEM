package com.todolist.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
  private final JwtUtils jwtUtils;

  public JwtAuthFilter(JwtUtils jwtUtils) {
    this.jwtUtils = jwtUtils;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7).trim();
    if (token.isEmpty()) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      Claims claims = jwtUtils.parseClaims(token);
      Collection<SimpleGrantedAuthority> authorities =
          jwtAuthorityStrings(claims.get("authorities")).stream().map(SimpleGrantedAuthority::new).toList();

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (JwtException ex) {
      // Не логируем JWT целиком.
      log.warn("JWT validation failed, token={}", maskToken(token));
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }

  private List<String> jwtAuthorityStrings(Object raw) {
    if (raw == null) {
      return List.of();
    }
    if (raw instanceof List<?> list) {
      return list.stream()
          .flatMap(o -> expandAuthorityLeaf(o))
          .toList();
    }
    return expandAuthorityLeaf(raw).toList();
  }

  /** JJWT может десериализовать claim как строки или вложенные структуры — нормализуем во flat list. */
  private Stream<String> expandAuthorityLeaf(Object o) {
    if (o == null) {
      return Stream.empty();
    }
    if (o instanceof String s) {
      return Stream.of(s);
    }
    // редкий случай: authority как map (напр. Gson/Jackson объект)
    if (o instanceof java.util.Map<?, ?> m && m.containsKey("authority")) {
      Object a = m.get("authority");
      return a != null ? Stream.of(a.toString()) : Stream.empty();
    }
    return Stream.empty();
  }

  private String maskToken(String token) {
    if (token == null || token.isBlank()) {
      return "<empty>";
    }
    if (token.length() <= 12) {
      return "******";
    }
    return token.substring(0, 6) + "..." + token.substring(token.length() - 6);
  }
}
