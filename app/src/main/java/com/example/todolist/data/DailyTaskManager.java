package com.example.todolist.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.todolist.model.DailyTask;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyTaskManager {
    private static final String PREF_NAME = "DailyTaskPrefs";
    private static final String KEY_LAST_CHECKED_DATE = "last_checked_date"; // 改为记录最后检查日期
    private static final String KEY_LAST_WEEK_NUMBER = "last_week_number";
    private static final String KEY_LAST_YEAR = "last_year";
    private static final String KEY_LAST_CHECKED_WEEK = "last_checked_week";
    private static final String KEY_LAST_CHECKED_YEAR = "last_checked_year";

    private DailyTaskDao dailyTaskDao;
    private List<DailyTask> dailyTaskList;
    private SharedPreferences sharedPreferences;
    private Context context;

    public DailyTaskManager(Context context) {
        this.context = context;
        dailyTaskDao = new DailyTaskDao(context);
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadData();
        checkAndResetDailyTasks();
        checkAndRollWeeklyData(); // 添加周数据滚动检查
    }

    // 从数据库加载数据
    private void loadData() {
        dailyTaskList = dailyTaskDao.getAllTasks();
    }

    // 检查并重置每日任务状态
    private void checkAndResetDailyTasks() {
        String today = getCurrentDate();
        boolean needsSave = false;

        for (DailyTask task : dailyTaskList) {
            if (task.needsReset(today)) {
                task.resetCompletion();
                dailyTaskDao.updateTask(task);
                needsSave = true;
            }
        }

        // 检查是否需要周数据滚动
        checkAndRollWeeklyData();
    }

    // 检查并滚动周数据
    private void checkAndRollWeeklyData() {
        int currentWeek = getCurrentWeekNumber();
        int currentYear = getCurrentYear();
        String today = getCurrentDate();

        int lastWeek = sharedPreferences.getInt(KEY_LAST_WEEK_NUMBER, -1);
        int lastYear = sharedPreferences.getInt(KEY_LAST_YEAR, -1);
        String lastCheckedDate = sharedPreferences.getString(KEY_LAST_CHECKED_DATE, "");

        // 如果上次检查不是今天，才需要检查周滚动
        if (!today.equals(lastCheckedDate)) {
            // 保存今天的检查日期
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_LAST_CHECKED_DATE, today);
            editor.apply();

            // 如果是新的一周（考虑跨年情况）
            boolean isNewWeek = (currentYear > lastYear) ||
                    (currentYear == lastYear && currentWeek > lastWeek);

            if (isNewWeek) {
                // 对所有任务执行周数据滚动
                for (DailyTask task : dailyTaskList) {
                    task.rollWeeklyData();
                    dailyTaskDao.updateTask(task);
                }

                // 保存当前周和年份
                SharedPreferences.Editor editor2 = sharedPreferences.edit();
                editor2.putInt(KEY_LAST_WEEK_NUMBER, currentWeek);
                editor2.putInt(KEY_LAST_YEAR, currentYear);
                editor2.apply();
            } else if (lastWeek == -1 || lastYear == -1) {
                // 第一次运行，保存当前周和年份
                SharedPreferences.Editor editor2 = sharedPreferences.edit();
                editor2.putInt(KEY_LAST_WEEK_NUMBER, currentWeek);
                editor2.putInt(KEY_LAST_YEAR, currentYear);
                editor2.apply();
            }
        }
    }

    // 滚动所有任务的周数据
    private void rollAllTasksWeeklyData() {
        for (DailyTask task : dailyTaskList) {
            task.rollWeeklyData();
            dailyTaskDao.updateTask(task); // 更新到数据库
        }
    }

    // 保存当前周信息
    private void saveCurrentWeekInfo(int week, int year) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_LAST_CHECKED_WEEK, week);
        editor.putInt(KEY_LAST_CHECKED_YEAR, year);
        editor.apply();
    }

    // 获取当前年份
    private int getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
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
        long newId = dailyTaskDao.insertTask(task);
        if (newId != -1) {
            task.setId((int) newId); // 设置数据库生成的新ID
            dailyTaskList.add(0, task); // 添加到内存列表
        }
    }

    public void updateTask(DailyTask task) {
        dailyTaskDao.updateTask(task);
        // 内存中的列表已经引用同一个对象，不需要额外操作
    }

    public void deleteTask(DailyTask task) {
        dailyTaskDao.deleteTask(task);
        dailyTaskList.remove(task);
    }

    public int getNextTaskId() {
        // 数据库使用自增ID，这里返回-1，实际ID由数据库生成
        return -1;
    }

    // 标记任务完成或取消完成
    public void markTaskCompleted(DailyTask task, boolean completed) {
        // 先检查是否需要滚动周数据
        checkAndRollWeeklyData();

        task.setCompletedToday(completed);

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 周日=0, 周一=1, ...

        if (completed) {
            // 设置完成日期
            task.setLastCompletedDate(getCurrentDate());
            // 记录到当前周（第0周）
            task.markCompleted(0, dayOfWeek);
        } else {
            // 取消完成：清除当天的完成记录
            task.unmarkCompleted(0, dayOfWeek);
        }

        updateTask(task);
    }

    // 保存数据（现在每次操作都立即保存到数据库，这个方法可以保留但不一定需要）
    public void saveData() {
        // 数据库操作是实时的，不需要批量保存
    }
}