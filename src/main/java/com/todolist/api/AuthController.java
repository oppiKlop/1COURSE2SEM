package com.todolist.api;

import com.todolist.dto.LoginRequest;
import com.todolist.dto.LoginResponse;
import com.todolist.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final String pepper;

  public AuthController(
      AuthenticationManager authenticationManager,
      JwtUtils jwtUtils,
      @Value("${app.security.pepper}") String pepper
  ) {
    this.authenticationManager = authenticationManager;
    this.jwtUtils = jwtUtils;
    this.pepper = pepper;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getUsername(),
            request.getPassword() + pepper
        )
    );

    String token = jwtUtils.generateToken(authentication.getName(), authentication.getAuthorities());
    return ResponseEntity.ok(new LoginResponse(token, "Bearer", jwtUtils.getTtlSeconds()));
  }
}
