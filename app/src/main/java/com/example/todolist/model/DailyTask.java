package com.example.todolist.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DailyTask {
    private int id;
    private String content;
    private boolean completedToday;
    private String lastCompletedDate; // 记录最后完成日期
    private List<int[]> weeklyCompletion;

    public DailyTask(int id, String content) {
        this.id = id;
        this.content = content;
        this.completedToday = false;
        this.lastCompletedDate = "";
        this.weeklyCompletion = new ArrayList<>();
        // 初始化10周的数据
        for (int i = 0; i < 10; i++) {
            weeklyCompletion.add(new int[7]); // 每周7天
        }
    }

    // 从JSON对象创建DailyTask
    public static DailyTask fromJson(JSONObject json) throws JSONException {
        System.out.println("=== DailyTask.fromJson START ===");

        int id = json.getInt("id");
        String content = json.getString("content");
        System.out.println("Creating task ID: " + id + ", Content: " + content);

        DailyTask task = new DailyTask(id, content);
        task.setCompletedToday(json.getBoolean("completedToday"));
        task.setLastCompletedDate(json.getString("lastCompletedDate"));
        System.out.println("Basic properties set");

        // 解析weeklyCompletion
        JSONArray weeksArray = json.getJSONArray("weeklyCompletion");
        List<int[]> weeklyCompletion = new ArrayList<>();
        System.out.println("Weekly completion array length: " + weeksArray.length());

        for (int i = 0; i < weeksArray.length(); i++) {
            JSONArray weekArray = weeksArray.getJSONArray(i);
            int[] week = new int[7];
            for (int j = 0; j < weekArray.length(); j++) {
                week[j] = weekArray.getInt(j);
            }
            weeklyCompletion.add(week);
            System.out.println("Added week " + i + " data");
        }
        task.setWeeklyCompletion(weeklyCompletion);

        System.out.println("=== DailyTask.fromJson END ===");
        return task;
    }

    // 将DailyTask转换为JSON对象
    public JSONObject toJson() throws JSONException {
        System.out.println("=== DailyTask.toJson START ===");

        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("content", content);
        json.put("completedToday", completedToday);
        json.put("lastCompletedDate", lastCompletedDate);
        System.out.println("Basic properties added to JSON");

        JSONArray weeksArray = new JSONArray();
        for (int[] week : weeklyCompletion) {
            JSONArray weekArray = new JSONArray();
            for (int day : week) {
                weekArray.put(day);
            }
            weeksArray.put(weekArray);
        }
        json.put("weeklyCompletion", weeksArray);
        System.out.println("Weekly completion data added to JSON");

        System.out.println("=== DailyTask.toJson END ===");
        return json;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isCompletedToday() { return completedToday; }
    public void setCompletedToday(boolean completed) { this.completedToday = completed; }
    public String getLastCompletedDate() { return lastCompletedDate; }
    public void setLastCompletedDate(String lastCompletedDate) { this.lastCompletedDate = lastCompletedDate; }
    public List<int[]> getWeeklyCompletion() { return weeklyCompletion; }
    public void setWeeklyCompletion(List<int[]> weeklyCompletion) { this.weeklyCompletion = weeklyCompletion; }

    // 标记某天完成
    public void markCompleted(int weekIndex, int dayOfWeek) {
        System.out.println("=== DailyTask.markCompleted START ===");
        System.out.println("Week index: " + weekIndex + ", Day of week: " + dayOfWeek);
        System.out.println("Weekly completion size: " + weeklyCompletion.size());

        if (weekIndex >= 0 && weekIndex < weeklyCompletion.size()) {
            int[] week = weeklyCompletion.get(weekIndex);
            System.out.println("Week array length: " + week.length);

            if (dayOfWeek >= 0 && dayOfWeek < 7) {
                week[dayOfWeek]++;
                System.out.println("Marked completed. New value: " + week[dayOfWeek]);
            } else {
                System.out.println("ERROR: Invalid day of week: " + dayOfWeek);
            }
        } else {
            System.out.println("ERROR: Invalid week index: " + weekIndex);
        }

        System.out.println("=== DailyTask.markCompleted END ===");
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

    // 检查是否需要重置完成状态（新的一天）
    public boolean needsReset(String today) {
        return !today.equals(lastCompletedDate);
    }

    // 重置完成状态
    public void resetCompletion() {
        this.completedToday = false;
    }

    // 周数据滚动（当新的一周开始时）
    public void rollWeeklyData() {
        if (weeklyCompletion.size() >= 10) {
            // 移除最旧的一周
            weeklyCompletion.remove(9);
            // 在开头添加新的一周
            weeklyCompletion.add(0, new int[7]);
        }
    }
}