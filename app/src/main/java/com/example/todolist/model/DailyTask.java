package com.example.todolist.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DailyTask implements Serializable {
    private int id;
    private String content;
    private boolean completedToday;
    private String lastCompletedDate; // 记录最后完成日期
    private List<int[]> weeklyCompletion;
    private static final long serialVersionUID = 1L;

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
        int id = json.getInt("id");
        String content = json.getString("content");
        DailyTask task = new DailyTask(id, content);
        task.setCompletedToday(json.getBoolean("completedToday"));
        task.setLastCompletedDate(json.getString("lastCompletedDate"));

        // 解析weeklyCompletion
        JSONArray weeksArray = json.getJSONArray("weeklyCompletion");
        List<int[]> weeklyCompletion = new ArrayList<>();
        for (int i = 0; i < weeksArray.length(); i++) {
            JSONArray weekArray = weeksArray.getJSONArray(i);
            int[] week = new int[7];
            for (int j = 0; j < weekArray.length(); j++) {
                week[j] = weekArray.getInt(j);
            }
            weeklyCompletion.add(week);
        }
        task.setWeeklyCompletion(weeklyCompletion);

        return task;
    }

    // 将DailyTask转换为JSON对象
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("content", content);
        json.put("completedToday", completedToday);
        json.put("lastCompletedDate", lastCompletedDate);

        JSONArray weeksArray = new JSONArray();
        for (int[] week : weeklyCompletion) {
            JSONArray weekArray = new JSONArray();
            for (int day : week) {
                weekArray.put(day);
            }
            weeksArray.put(weekArray);
        }
        json.put("weeklyCompletion", weeksArray);

        return json;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
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
        if (weekIndex >= 0 && weekIndex < weeklyCompletion.size()) {
            int[] week = weeklyCompletion.get(weekIndex);
            if (dayOfWeek >= 0 && dayOfWeek < 7) {
                week[dayOfWeek]++;
            }
        }
    }

    // 取消某天的完成记录
    public void unmarkCompleted(int weekIndex, int dayOfWeek) {
        if (weekIndex >= 0 && weekIndex < weeklyCompletion.size()) {
            int[] week = weeklyCompletion.get(weekIndex);
            if (dayOfWeek >= 0 && dayOfWeek < 7 && week[dayOfWeek] > 0) {
                week[dayOfWeek]--;
            }
        }
    }

    // 获取某周的完成次数
//    public int getWeekCompletionCount(int weekIndex) {
//        if (weekIndex >= 0 && weekIndex < weeklyCompletion.size()) {
//            int[] week = weeklyCompletion.get(weekIndex);
//            int count = 0;
//            for (int completion : week) {
//                if (completion > 0) count++;
//            }
//            return count;
//        }
//        return 0;
//    }

    // 获取某周的总完成次数（所有天完成次数的总和）
    public int getWeekTotalCompletionCount(int weekIndex) {
        if (weekIndex >= 0 && weekIndex < weeklyCompletion.size()) {
            int[] week = weeklyCompletion.get(weekIndex);
            int total = 0;
            for (int completion : week) {
                total += completion;
            }
            return total;
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
        if (weeklyCompletion == null) {
            weeklyCompletion = new ArrayList<>();
        }

        // 确保有10周的数据
        while (weeklyCompletion.size() < 10) {
            weeklyCompletion.add(new int[7]);
        }

        if (weeklyCompletion.size() >= 10) {
            // 在开头添加新的一周
            weeklyCompletion.add(0, new int[7]);

            // 如果超过10周，移除最旧的一周
            if (weeklyCompletion.size() > 10) {
                weeklyCompletion.remove(weeklyCompletion.size() - 1);
            }
        }
    }

    // 添加方法：获取指定周的完成情况（用于显示）
    public int[] getWeekCompletion(int weekIndex) {
        if (weekIndex >= 0 && weekIndex < weeklyCompletion.size()) {
            return weeklyCompletion.get(weekIndex);
        }
        return new int[7]; // 返回空数组
    }

    // 添加方法：获取最近n周的完成统计
    public List<int[]> getRecentWeeks(int weeksCount) {
        int actualCount = Math.min(weeksCount, weeklyCompletion.size());
        return weeklyCompletion.subList(0, actualCount);
    }
}