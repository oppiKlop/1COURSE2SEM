package com.todolist.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestScopedBean {
    private final String requestId;
    private final LocalDateTime startTime;

    public RequestScopedBean() {
        this.requestId = UUID.randomUUID().toString();
        this.startTime = LocalDateTime.now();
        System.out.println("СОЗДАН НОВЫЙ RequestScopedBean: " + requestId);
    }

    public String getRequestId() {
        return requestId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public long getProcessingTime() {
        return java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
    }
}