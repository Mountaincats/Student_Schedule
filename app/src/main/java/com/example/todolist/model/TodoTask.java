package com.example.todolist.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class TodoTask {
    private int id;
    private String content;
    private int priority; // 优先级，数值越小优先级越高
    private long createdTime;

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

    // 从JSON对象创建TodoTask
    public static TodoTask fromJson(JSONObject json) throws JSONException {
        int id = json.getInt("id");
        String content = json.getString("content");
        int priority = json.getInt("priority");
        long createdTime = json.getLong("createdTime");

        return new TodoTask(id, content, priority, createdTime);
    }

    // 将TodoTask转换为JSON对象
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("content", content);
        json.put("priority", priority);
        json.put("createdTime", createdTime);
        return json;
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

    // 提升优先级（数值减小）
    public void increasePriority() {
        if (priority > 0) {
            priority--;
        }
    }

    // 降低优先级（数值增加）
    public void decreasePriority() {
        priority++;
    }
}