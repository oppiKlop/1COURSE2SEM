package com.todolist.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
  private final SecretKey key;
  private final Duration ttl;

  public JwtUtils(
      @Value("${app.security.jwt.secret}") String secret,
      @Value("${app.security.jwt.ttl-seconds:3600}") long ttlSeconds
  ) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.ttl = Duration.ofSeconds(ttlSeconds);
  }

  public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
    Instant now = Instant.now();
    List<String> authorityNames = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

    return Jwts.builder()
        .subject(username)
        .claim("authorities", authorityNames)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(ttl)))
        .signWith(key)
        .compact();
  }

  public Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public long getTtlSeconds() {
    return ttl.toSeconds();
  }
}
