package com.example.todolist.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DailyTask {
    private int id;
    private String content;
    private boolean completedToday;
    private List<int[]> weeklyCompletion; // 存储最近10周的完成情况

    public DailyTask(int id, String content) {
        this.id = id;
        this.content = content;
        this.completedToday = false;
        this.weeklyCompletion = new ArrayList<>();
        // 初始化10周的数据
        for (int i = 0; i < 10; i++) {
            weeklyCompletion.add(new int[7]); // 每周7天
        }
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isCompletedToday() { return completedToday; }
    public void setCompletedToday(boolean completed) { this.completedToday = completed; }
    public List<int[]> getWeeklyCompletion() { return weeklyCompletion; }

    // 标记某天完成
    public void markCompleted(int weekIndex, int dayOfWeek) {
        if (weekIndex >= 0 && weekIndex < weeklyCompletion.size()) {
            int[] week = weeklyCompletion.get(weekIndex);
            if (dayOfWeek >= 0 && dayOfWeek < 7) {
                week[dayOfWeek]++;
            }
        }
    }

    // 获取某周的完成次数
    public int getWeekCompletionCount(int weekIndex) {
        if (weekIndex >= 0 && weekIndex < weeklyCompletion.size()) {
            int[] week = weeklyCompletion.get(weekIndex);
            int count = 0;
            for (int completion : week) {
                if (completion > 0) count++;
            }
            return count;
        }
        return 0;
    }
}