package com.example.todolist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TodoDbHelper extends SQLiteOpenHelper {
    // 数据库信息
    private static final String DATABASE_NAME = "todotasks.db";
    private static final int DATABASE_VERSION = 1;

    // 表名
    public static final String TABLE_TODO_TASKS = "todo_tasks";

    // 列名
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_PRIORITY = "priority";
    public static final String COLUMN_CREATED_TIME = "created_time";

    // 创建表的SQL语句
    private static final String SQL_CREATE_TODO_TASKS_TABLE =
            "CREATE TABLE " + TABLE_TODO_TASKS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CONTENT + " TEXT NOT NULL, " +
                    COLUMN_PRIORITY + " INTEGER DEFAULT 0, " +
                    COLUMN_CREATED_TIME + " INTEGER NOT NULL);";

    public TodoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TODO_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO_TASKS);
        onCreate(db);
    }
}