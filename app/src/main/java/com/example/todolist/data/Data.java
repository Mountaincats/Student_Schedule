package com.example.todolist.data;

import android.content.Context;
import com.example.todolist.model.Day;
import com.example.todolist.model.Schedule;
import com.example.todolist.model.Week;
import java.time.LocalDate;
import java.util.List;

public class Data {
    private static Data instance;
    private AppDatabase db;

    private Data(Context context) {
        db = AppDatabase.getDatabase(context);
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new Data(context);
        }
    }

    public static Data getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Data class must be initialized with init(Context) first");
        }
        return instance;
    }

    /**
     * 加载某一周的数据，其实就是加载这一周每一天的数据
     * 注意：这会将数据库中的数据填充到 Week 对象中。如果 Week 对象中已有数据，可能会导致重复，建议传入新的 Week 对象。
     */
    public void loadWeekData(Week week) {
        LocalDate monday = week.getMonday();
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            // 从数据库加载当天的日程
            List<ScheduleEntity> entities = db.scheduleDao().getSchedulesForDate(date);
            
            // 找到 Week 中对应的那一天（如果没有则创建并添加）
            Day day = week.getDayForDate(date);
            if (day == null) {
                day = new Day(date);
                week.addDay(day);
            }
            
            // 如果 Day 类没有清空日程的方法，这里我们假设我们是增量添加，或者调用者保证 Day 是干净的
            // 由于 Day.java 中只有 addSchedule，我们无法清空。
            // 实际应用中，建议在 Day 中添加 clearSchedules() 方法。
            
            for (ScheduleEntity entity : entities) {
                Schedule.Note note = new Schedule.Note(entity.noteName, entity.noteContent);
                Schedule schedule = new Schedule(
                        entity.startTime, 
                        entity.endTime, 
                        entity.name, 
                        note, 
                        entity.colorArgb, 
                        entity.isTemporarySchedule
                );
                // 避免重复添加：这里可以做一个简单的检查，或者依赖上层的逻辑
                if (!day.getSchedules().contains(schedule)) {
                    day.addSchedule(schedule);
                }
            }
        }
    }

    /**
     * 保存某一天的数据
     */
    public void saveDay(Day day) {
        if (day == null) return;
        
        // 先删除旧数据（全量覆盖策略）
        db.scheduleDao().deleteAllSchedulesForDate(day.getDate());
        
        // 插入新数据
        for (Schedule schedule : day.getSchedules()) {
            ScheduleEntity entity = new ScheduleEntity(
                    schedule.getStartTime(),
                    schedule.getEndTime(),
                    schedule.getName(),
                    schedule.getNote().getName(),
                    schedule.getNote().getContent(),
                    schedule.getColorArgb(),
                    schedule.isTemporarySchedule(),
                    day.getDate()
            );
            db.scheduleDao().insert(entity);
        }
    }
    
    /**
     * 保存整周的数据
     */
    public void saveWeek(Week week) {
        if (week == null) return;
        for (List<Day> dayList : week.getDays()) {
            for (Day day : dayList) {
                saveDay(day);
            }
        }
    }
}