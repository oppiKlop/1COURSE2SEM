package com.todolist.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseCookie;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/preferences")
public class PreferencesController {

  private final String apiVersion;

  public PreferencesController(@Value("${app.version}") String apiVersion) {
    this.apiVersion = apiVersion;
  }

  @GetMapping("/view")
  @Operation(summary = "Get view preference")
  @ApiResponse(responseCode = "200", description = "Preference returned")
  public ResponseEntity<String> getViewPreference(
      @CookieValue(name = "viewPreference", required = false) String mode,
      HttpServletResponse response
  ) {
    String actual = mode == null ? "compact" : mode;
    if (mode == null) {
      addCookie(response, actual);
    }

    return ResponseEntity.ok()
        .header("X-API-Version", apiVersion)
        .body(actual);
  }

  @PostMapping("/view")
  @Operation(summary = "Set view preference")
  @ApiResponse(responseCode = "200", description = "Preference updated")
  public ResponseEntity<String> setViewPreference(
      @RequestParam("mode") String mode,
      HttpServletResponse response
  ) {
    if (!"compact".equals(mode) && !"detailed".equals(mode)) {
      return ResponseEntity.badRequest()
          .header("X-API-Version", apiVersion)
          .body("mode must be compact|detailed");
    }

    addCookie(response, mode);

    return ResponseEntity.ok()
        .header("X-API-Version", apiVersion)
        .body(mode);
  }

  private void addCookie(HttpServletResponse response, String mode) {
    ResponseCookie cookie = ResponseCookie.from("viewPreference", mode)
        .path("/")
        .httpOnly(false)
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}

