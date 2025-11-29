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
    private void loadData() {
        String tasksJson = prefs.getString(KEY_TASKS, "[]");
        taskIdCounter = prefs.getInt(KEY_TASK_ID_COUNTER, 0);

        try {
            JSONArray tasksArray = new JSONArray(tasksJson);
            for (int i = 0; i < tasksArray.length(); i++) {
                JSONObject taskJson = tasksArray.getJSONObject(i);
                DailyTask task = DailyTask.fromJson(taskJson);
                dailyTaskList.add(task);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 保存数据
    public void saveData() {
        try {
            JSONArray tasksArray = new JSONArray();
            for (DailyTask task : dailyTaskList) {
                tasksArray.put(task.toJson());
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_TASKS, tasksArray.toString());
            editor.putInt(KEY_TASK_ID_COUNTER, taskIdCounter);
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
    public void markTaskCompleted(DailyTask task, boolean completed) {
        task.setCompletedToday(completed);

        if (completed) {
            // 设置完成日期
            task.setLastCompletedDate(getCurrentDate());

            // 记录到当前周
            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 周日=0, 周一=1, ...
            task.markCompleted(0, dayOfWeek);
        }

        updateTask(task);
    }
}