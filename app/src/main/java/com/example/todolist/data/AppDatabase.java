package com.example.todolist.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {ScheduleEntity.class, DayEntity.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract ScheduleDao scheduleDao();
    public abstract DayDao dayDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "schedule_database")
                            .fallbackToDestructiveMigration() // 注意：这将清除旧数据！开发阶段可以使用。
                            .allowMainThreadQueries() 
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}