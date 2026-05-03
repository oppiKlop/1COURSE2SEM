package com.todolist.api;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ProfileController {
  @GetMapping("/profile")
  public ResponseEntity<Map<String, Object>> profile(Authentication authentication) {
    return ResponseEntity.ok(Map.of(
        "username", authentication.getName(),
        "authorities", authentication.getAuthorities().stream().map(Object::toString).toList()
    ));
  }

  @GetMapping("/docs")
  public ResponseEntity<Map<String, String>> docs() {
    return ResponseEntity.ok(Map.of("message", "Restricted docs endpoint"));
  }
}
