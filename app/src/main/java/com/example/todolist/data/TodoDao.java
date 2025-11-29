package com.example.todolist.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.todolist.model.TodoTask;
import java.util.ArrayList;
import java.util.List;

public class TodoDao {
    private TodoDbHelper dbHelper;

    public TodoDao(Context context) {
        dbHelper = new TodoDbHelper(context);
    }

    // 插入新任务
    public long insertTask(TodoTask task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TodoDbHelper.COLUMN_CONTENT, task.getContent());
        values.put(TodoDbHelper.COLUMN_PRIORITY, task.getPriority());
        values.put(TodoDbHelper.COLUMN_CREATED_TIME, task.getCreatedTime());

        long id = db.insert(TodoDbHelper.TABLE_TODO_TASKS, null, values);
        db.close();
        return id;
    }

    // 更新任务
    public int updateTask(TodoTask task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TodoDbHelper.COLUMN_CONTENT, task.getContent());
        values.put(TodoDbHelper.COLUMN_PRIORITY, task.getPriority());

        int count = db.update(TodoDbHelper.TABLE_TODO_TASKS, values,
                TodoDbHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
        db.close();
        return count;
    }

    // 删除任务
    public int deleteTask(TodoTask task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = db.delete(TodoDbHelper.TABLE_TODO_TASKS,
                TodoDbHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
        db.close();
        return count;
    }

    // 获取所有任务（按优先级升序，创建时间降序）
    public List<TodoTask> getAllTasks() {
        List<TodoTask> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                TodoDbHelper.COLUMN_ID,
                TodoDbHelper.COLUMN_CONTENT,
                TodoDbHelper.COLUMN_PRIORITY,
                TodoDbHelper.COLUMN_CREATED_TIME
        };

        // 按优先级升序，创建时间降序排序
        String orderBy = TodoDbHelper.COLUMN_PRIORITY + " ASC, " +
                TodoDbHelper.COLUMN_CREATED_TIME + " DESC";

        Cursor cursor = db.query(
                TodoDbHelper.TABLE_TODO_TASKS,
                projection,
                null, null, null, null,
                orderBy
        );

        if (cursor.moveToFirst()) {
            do {
                TodoTask task = cursorToTodoTask(cursor);
                if (task != null) {
                    tasks.add(task);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return tasks;
    }

    // 将Cursor转换为TodoTask对象
    private TodoTask cursorToTodoTask(Cursor cursor) {
        try {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(TodoDbHelper.COLUMN_ID));
            String content = cursor.getString(cursor.getColumnIndexOrThrow(TodoDbHelper.COLUMN_CONTENT));
            int priority = cursor.getInt(cursor.getColumnIndexOrThrow(TodoDbHelper.COLUMN_PRIORITY));
            long createdTime = cursor.getLong(cursor.getColumnIndexOrThrow(TodoDbHelper.COLUMN_CREATED_TIME));

            return new TodoTask(id, content, priority, createdTime);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}