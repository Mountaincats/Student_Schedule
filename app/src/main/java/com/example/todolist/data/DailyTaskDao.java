package com.example.todolist.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.todolist.model.DailyTask;
import org.json.JSONArray;
import org.json.JSONException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyTaskDao {
    private DailyTaskDbHelper dbHelper;

    public DailyTaskDao(Context context) {
        dbHelper = new DailyTaskDbHelper(context);
    }

    // 插入新任务
    public long insertTask(DailyTask task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DailyTaskDbHelper.COLUMN_CONTENT, task.getContent());
        values.put(DailyTaskDbHelper.COLUMN_COMPLETED_TODAY, task.isCompletedToday() ? 1 : 0);
        values.put(DailyTaskDbHelper.COLUMN_LAST_COMPLETED_DATE, task.getLastCompletedDate());
        values.put(DailyTaskDbHelper.COLUMN_CREATED_DATE, getCurrentDateTime());
        values.put(DailyTaskDbHelper.COLUMN_WEEKLY_DATA, weeklyCompletionToJson(task.getWeeklyCompletion()));

        long id = db.insert(DailyTaskDbHelper.TABLE_DAILY_TASKS, null, values);
        db.close();
        return id;
    }

    // 更新任务
    // 更新任务（包括内容、完成状态等）
    public int updateTask(DailyTask task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DailyTaskDbHelper.COLUMN_CONTENT, task.getContent());
        values.put(DailyTaskDbHelper.COLUMN_COMPLETED_TODAY, task.isCompletedToday() ? 1 : 0);
        values.put(DailyTaskDbHelper.COLUMN_LAST_COMPLETED_DATE, task.getLastCompletedDate());
        values.put(DailyTaskDbHelper.COLUMN_WEEKLY_DATA, weeklyCompletionToJson(task.getWeeklyCompletion()));

        int count = db.update(DailyTaskDbHelper.TABLE_DAILY_TASKS, values,
                DailyTaskDbHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
        db.close();
        return count;
    }


    // 删除任务
    public int deleteTask(DailyTask task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = db.delete(DailyTaskDbHelper.TABLE_DAILY_TASKS,
                DailyTaskDbHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
        db.close();
        return count;
    }

    // 获取所有任务（按创建时间倒序）
    public List<DailyTask> getAllTasks() {
        List<DailyTask> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DailyTaskDbHelper.COLUMN_ID,
                DailyTaskDbHelper.COLUMN_CONTENT,
                DailyTaskDbHelper.COLUMN_COMPLETED_TODAY,
                DailyTaskDbHelper.COLUMN_LAST_COMPLETED_DATE,
                DailyTaskDbHelper.COLUMN_WEEKLY_DATA
        };

        Cursor cursor = db.query(
                DailyTaskDbHelper.TABLE_DAILY_TASKS,
                projection,
                null, null, null, null,
                DailyTaskDbHelper.COLUMN_CREATED_DATE + " DESC" // 按创建时间倒序
        );

        if (cursor.moveToFirst()) {
            do {
                DailyTask task = cursorToDailyTask(cursor);
                if (task != null) {
                    tasks.add(task);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return tasks;
    }

    // 获取单个任务
    public DailyTask getTaskById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DailyTaskDbHelper.COLUMN_ID,
                DailyTaskDbHelper.COLUMN_CONTENT,
                DailyTaskDbHelper.COLUMN_COMPLETED_TODAY,
                DailyTaskDbHelper.COLUMN_LAST_COMPLETED_DATE,
                DailyTaskDbHelper.COLUMN_WEEKLY_DATA
        };

        String selection = DailyTaskDbHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = db.query(
                DailyTaskDbHelper.TABLE_DAILY_TASKS,
                projection,
                selection, selectionArgs, null, null, null
        );

        DailyTask task = null;
        if (cursor.moveToFirst()) {
            task = cursorToDailyTask(cursor);
        }

        cursor.close();
        db.close();
        return task;
    }

    // 将Cursor转换为DailyTask对象
    private DailyTask cursorToDailyTask(Cursor cursor) {
        try {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DailyTaskDbHelper.COLUMN_ID));
            String content = cursor.getString(cursor.getColumnIndexOrThrow(DailyTaskDbHelper.COLUMN_CONTENT));
            boolean completedToday = cursor.getInt(cursor.getColumnIndexOrThrow(DailyTaskDbHelper.COLUMN_COMPLETED_TODAY)) == 1;
            String lastCompletedDate = cursor.getString(cursor.getColumnIndexOrThrow(DailyTaskDbHelper.COLUMN_LAST_COMPLETED_DATE));
            String weeklyDataJson = cursor.getString(cursor.getColumnIndexOrThrow(DailyTaskDbHelper.COLUMN_WEEKLY_DATA));

            DailyTask task = new DailyTask(id, content);
            task.setCompletedToday(completedToday);
            task.setLastCompletedDate(lastCompletedDate != null ? lastCompletedDate : "");
            task.setWeeklyCompletion(jsonToWeeklyCompletion(weeklyDataJson));

            return task;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 将weeklyCompletion列表转换为JSON字符串
    private String weeklyCompletionToJson(List<int[]> weeklyCompletion) {
        if (weeklyCompletion == null) return "[]";

        JSONArray jsonArray = new JSONArray();
        for (int[] week : weeklyCompletion) {
            JSONArray weekArray = new JSONArray();
            for (int day : week) {
                weekArray.put(day);
            }
            jsonArray.put(weekArray);
        }
        return jsonArray.toString();
    }

    // 将JSON字符串转换为weeklyCompletion列表
    private List<int[]> jsonToWeeklyCompletion(String jsonStr) {
        List<int[]> weeklyCompletion = new ArrayList<>();

        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            // 返回默认的10周空数据
            for (int i = 0; i < 10; i++) {
                weeklyCompletion.add(new int[7]);
            }
            return weeklyCompletion;
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray weekArray = jsonArray.getJSONArray(i);
                int[] week = new int[7];
                for (int j = 0; j < weekArray.length() && j < 7; j++) {
                    week[j] = weekArray.getInt(j);
                }
                weeklyCompletion.add(week);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // 如果解析失败，返回默认的10周空数据
            for (int i = 0; i < 10; i++) {
                weeklyCompletion.add(new int[7]);
            }
        }

        return weeklyCompletion;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}