package com.todolist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PreferencesController.class)
class TestPreferencesController {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getViewPreference_shouldSetDefaultCookie_WhenMissing() throws Exception {
    mockMvc.perform(get("/api/preferences/view"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-API-Version", "2.0.0"))
        .andExpect(content().string("compact"))
        .andExpect(cookie().exists("viewPreference"));
  }

  @Test
  void setViewPreference_shouldUpdateCookie() throws Exception {
    mockMvc.perform(post("/api/preferences/view")
            .param("mode", "detailed"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-API-Version", "2.0.0"))
        .andExpect(content().string("detailed"))
        .andExpect(cookie().value("viewPreference", "detailed"));
  }

  @Test
  void setViewPreference_shouldReturn400_WhenModeInvalid() throws Exception {
    mockMvc.perform(post("/api/preferences/view")
            .param("mode", "invalid"))
        .andExpect(status().isBadRequest())
        .andExpect(header().string("X-API-Version", "2.0.0"));
  }
}

