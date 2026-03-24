package com.todolist.controller;

import com.todolist.dto.Task;
import com.todolist.scope.PrototypeScopedBean;
import com.todolist.scope.RequestScopedBean;
import org.springframework.web.bind.annotation.*;
import com.todolist.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    private final RequestScopedBean requestScopedBean;
    private final PrototypeScopedBean prototypeScopedBean;

    public TaskController(TaskService taskService,
                          RequestScopedBean requestScopedBean,
                          PrototypeScopedBean prototypeScopedBean) {
        this.taskService = taskService;
        this.requestScopedBean = requestScopedBean;
        this.prototypeScopedBean = prototypeScopedBean;
    }

    @GetMapping
    public List<Task> getAllTasks() {

        System.out.println("REQUEST SCOPED BEAN");
        System.out.println("Request ID: " + requestScopedBean.getRequestId());
        System.out.println("Start time: " + requestScopedBean.getStartTime());

        System.out.println("PROTOTYPE SCOPED BEAN");
        System.out.println("Instance ID: " + prototypeScopedBean.getInstanceId());
        System.out.println("Generated Task ID: " + prototypeScopedBean.generateTaskId());

        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTask(id);
    }

    @PostMapping
    public Task createTask(@RequestBody String description, String title) {
        return taskService.insertTask(description, title);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody String title, String description, boolean completed) {
        return taskService.updateTask(id, description, title, completed);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}