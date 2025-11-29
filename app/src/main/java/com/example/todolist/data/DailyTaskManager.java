package com.example.todolist.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.todolist.model.DailyTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyTaskManager {
    private static final String PREFS_NAME = "DailyTasksPrefs";
    private static final String KEY_TASKS = "daily_tasks";
    private static final String KEY_TASK_ID_COUNTER = "task_id_counter";
    private static final String KEY_LAST_ACCESS_DATE = "last_access_date";
    private static final String KEY_LAST_WEEK_NUMBER = "last_week_number";

    private SharedPreferences prefs;
    private List<DailyTask> dailyTaskList;
    private int taskIdCounter;

    public DailyTaskManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        dailyTaskList = new ArrayList<>();
        loadData();
        checkAndResetDailyTasks();
    }

    // 加载数据
// 加载数据
    private void loadData() {
        System.out.println("=== DailyTaskManager.loadData START ===");

        String tasksJson = prefs.getString(KEY_TASKS, "[]");
        taskIdCounter = prefs.getInt(KEY_TASK_ID_COUNTER, 0);
        System.out.println("Loaded JSON: " + tasksJson);
        System.out.println("Task ID counter: " + taskIdCounter);

        try {
            JSONArray tasksArray = new JSONArray(tasksJson);
            System.out.println("Number of tasks in JSON: " + tasksArray.length());

            for (int i = 0; i < tasksArray.length(); i++) {
                System.out.println("Processing task at index: " + i);
                JSONObject taskJson = tasksArray.getJSONObject(i);
                DailyTask task = DailyTask.fromJson(taskJson);
                dailyTaskList.add(task);
                System.out.println("Added task ID: " + task.getId());
            }
        } catch (JSONException e) {
            System.out.println("ERROR in loadData JSON: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("ERROR in loadData: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== DailyTaskManager.loadData END ===");
    }

    // 保存数据
// 保存数据
    public void saveData() {
        System.out.println("=== DailyTaskManager.saveData START ===");
        System.out.println("Number of tasks to save: " + dailyTaskList.size());

        try {
            JSONArray tasksArray = new JSONArray();
            for (DailyTask task : dailyTaskList) {
                System.out.println("Processing task ID: " + task.getId());
                tasksArray.put(task.toJson());
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_TASKS, tasksArray.toString());
            editor.putInt(KEY_TASK_ID_COUNTER, taskIdCounter);
            boolean success = editor.commit(); // 使用commit而不是apply，以便立即看到结果
            System.out.println("Save to SharedPreferences successful: " + success);
        } catch (JSONException e) {
            System.out.println("ERROR in saveData JSON: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("ERROR in saveData: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== DailyTaskManager.saveData END ===");
    }

    // 检查并重置每日任务状态
    private void checkAndResetDailyTasks() {
        String today = getCurrentDate();
        String lastAccessDate = prefs.getString(KEY_LAST_ACCESS_DATE, "");

        // 如果是新的一天，重置所有任务的完成状态
        if (!today.equals(lastAccessDate)) {
            for (DailyTask task : dailyTaskList) {
                if (task.needsReset(today)) {
                    task.resetCompletion();
                }
            }

            // 检查是否需要周数据滚动
            checkAndRollWeeklyData();

            // 更新最后访问日期
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_LAST_ACCESS_DATE, today);
            editor.apply();
        }
    }

    // 检查并滚动周数据
    private void checkAndRollWeeklyData() {
        int currentWeek = getCurrentWeekNumber();
        int lastWeek = prefs.getInt(KEY_LAST_WEEK_NUMBER, currentWeek);

        // 如果是新的一周，滚动周数据
        if (currentWeek != lastWeek) {
            for (DailyTask task : dailyTaskList) {
                task.rollWeeklyData();
            }

            // 更新最后周数
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_LAST_WEEK_NUMBER, currentWeek);
            editor.apply();
        }
    }

    // 获取当前日期字符串（yyyy-MM-dd格式）
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    // 获取当前周数
    private int getCurrentWeekNumber() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    // 公共方法
    public List<DailyTask> getDailyTasks() {
        return dailyTaskList;
    }

    public void addTask(DailyTask task) {
        dailyTaskList.add(0, task);
        saveData();
    }

    public void updateTask(DailyTask task) {
        saveData();
    }

    public void deleteTask(DailyTask task) {
        dailyTaskList.remove(task);
        saveData();
    }

    public int getNextTaskId() {
        return taskIdCounter++;
    }

    // 标记任务完成
// 标记任务完成
    public void markTaskCompleted(DailyTask task, boolean completed) {
        System.out.println("=== DailyTaskManager.markTaskCompleted START ===");
        System.out.println("Task ID: " + task.getId());
        System.out.println("Completed: " + completed);

        task.setCompletedToday(completed);
        System.out.println("setCompletedToday executed");

        if (completed) {
            // 设置完成日期
            String currentDate = getCurrentDate();
            System.out.println("Current date: " + currentDate);
            task.setLastCompletedDate(currentDate);
            System.out.println("setLastCompletedDate executed");

            // 记录到当前周
            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 周日=0, 周一=1, ...
            System.out.println("Day of week: " + dayOfWeek);

            try {
                task.markCompleted(0, dayOfWeek);
                System.out.println("markCompleted executed for week 0, day " + dayOfWeek);
            } catch (Exception e) {
                System.out.println("ERROR in markCompleted: " + e.getMessage());
                e.printStackTrace();
            }
        }

        try {
            updateTask(task);
            System.out.println("updateTask executed successfully");
        } catch (Exception e) {
            System.out.println("ERROR in updateTask: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== DailyTaskManager.markTaskCompleted END ===");
    }
}