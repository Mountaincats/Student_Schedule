package com.example.todolist.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class TodoTask implements Serializable {
    private int id;
    private String content;
    private int priority; // 优先级，数值越小优先级越高
    private long createdTime;
    private static final long serialVersionUID = 1L;

    public TodoTask(int id, String content) {
        this.id = id;
        this.content = content;
        this.priority = 0; // 默认优先级
        this.createdTime = System.currentTimeMillis();
    }

    public TodoTask(int id, String content, int priority, long createdTime) {
        this.id = id;
        this.content = content;
        this.priority = priority;
        this.createdTime = createdTime;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
}