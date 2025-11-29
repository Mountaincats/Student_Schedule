package com.example.todolist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DailyTaskDbHelper extends SQLiteOpenHelper {
    // 数据库信息
    private static final String DATABASE_NAME = "dailytasks.db";
    private static final int DATABASE_VERSION = 1;

    // 表名
    public static final String TABLE_DAILY_TASKS = "daily_tasks";

    // 列名
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_COMPLETED_TODAY = "completed_today";
    public static final String COLUMN_LAST_COMPLETED_DATE = "last_completed_date";
    public static final String COLUMN_CREATED_DATE = "created_date";
    public static final String COLUMN_WEEKLY_DATA = "weekly_data"; // 存储为JSON字符串

    // 创建表的SQL语句
    private static final String SQL_CREATE_DAILY_TASKS_TABLE =
            "CREATE TABLE " + TABLE_DAILY_TASKS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CONTENT + " TEXT NOT NULL, " +
                    COLUMN_COMPLETED_TODAY + " INTEGER DEFAULT 0, " +
                    COLUMN_LAST_COMPLETED_DATE + " TEXT, " +
                    COLUMN_CREATED_DATE + " TEXT NOT NULL, " +
                    COLUMN_WEEKLY_DATA + " TEXT);";

    public DailyTaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DAILY_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 简单处理：删除旧表，创建新表（实际项目中需要更复杂的迁移逻辑）
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAILY_TASKS);
        onCreate(db);
    }
}