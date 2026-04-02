package com.todolist.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoritesController {
  @PostMapping("/{id}")
  public void add(@PathVariable Long id, HttpSession s) {
    List<Long> list = (List<Long>) s.getAttribute("favoriteTaskIds");
    if (list == null) list = new ArrayList<>();
    list.add(id);
    s.setAttribute("favoriteTaskIds", list);
  }
}
