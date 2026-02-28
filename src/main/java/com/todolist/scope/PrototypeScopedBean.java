package com.todolist.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope("prototype")
public class PrototypeScopedBean {
    private final String instanceId;

    public PrototypeScopedBean() {
        this.instanceId = UUID.randomUUID().toString();
        System.out.println("СОЗДАН НОВЫЙ PrototypeScopedBean: " + instanceId);
    }

    public String generateTaskId() {
        return "TASK: " + UUID.randomUUID().toString().substring(0, 8);
    }

    public String getInstanceId() {
        return instanceId;
    }
}