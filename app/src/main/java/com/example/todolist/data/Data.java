package com.example.todolist.data;

import android.content.Context;
import com.example.todolist.model.Day;
import com.example.todolist.model.RepeatRule;
import com.example.todolist.model.Schedule;
import com.example.todolist.model.Week;

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

    public void loadAllDataToWeek(Week week) {
        List<DayEntity> dayEntities = db.dayDao().getAllDays();

        for (DayEntity dayEntity : dayEntities) {
            RepeatRule repeatRule = new RepeatRule(
                    dayEntity.repeatMode,
                    dayEntity.repeatInterval,
                    dayEntity.repeatOccurrences,
                    dayEntity.repeatStartDate,
                    dayEntity.repeatEndDate
            );

            Day day = new Day(dayEntity.originDate, dayEntity.isTemporaryDay, repeatRule);
            day.setActiveHours(dayEntity.activeStartHour, dayEntity.activeEndHour);
            
            // 回写数据库 ID 到模型
            day.setDatabaseId(dayEntity.id);
            
            // 先清空，再加载，防止重复
            day.clearSchedules();
            
            List<ScheduleEntity> scheduleEntities = db.scheduleDao().getSchedulesForDayId(dayEntity.id);
            for (ScheduleEntity se : scheduleEntities) {
                Schedule.Note note = new Schedule.Note(se.noteName, se.noteContent);
                Schedule schedule = new Schedule(
                        se.startTime, se.endTime, se.name, note, se.colorArgb, se.isTemporarySchedule
                );
                day.addSchedule(schedule);
            }

            week.addDay(day);
        }
    }

    public void saveDay(Day day) {
        if (day == null) return;
        
        DayEntity entity = new DayEntity();
        // 同步模型数据到 Entity
        entity.originDate = day.getDate();
        entity.activeStartHour = day.getActiveStartHour();
        entity.activeEndHour = day.getActiveEndHour();
        entity.isTemporaryDay = day.isTemporaryDay();
        
        RepeatRule rr = day.getRepeatRule();
        entity.repeatMode = rr.getMode();
        entity.repeatInterval = rr.getInterval();
        entity.repeatOccurrences = rr.getOccurrences();
        entity.repeatStartDate = rr.getStartDate();
        entity.repeatEndDate = rr.getEndDate();

        long dayId = day.getDatabaseId();

        // 根据有无 ID，决定是更新还是插入
        if (dayId > 0) {
            entity.id = dayId;
            db.dayDao().update(entity);
        } else {
            dayId = db.dayDao().insert(entity);
            day.setDatabaseId(dayId); // 回写新生成的 ID
        }

        // 对于 Schedule，采用全量覆盖策略：先删后加
        db.scheduleDao().deleteAllSchedulesForDayId(dayId);

        for (Schedule schedule : day.getSchedules()) {
            ScheduleEntity se = new ScheduleEntity(
                    schedule.getStartTime(),
                    schedule.getEndTime(),
                    schedule.getName(),
                    schedule.getNote().getName(),
                    schedule.getNote().getContent(),
                    schedule.getColorArgb(),
                    schedule.isTemporarySchedule(),
                    dayId
            );
            db.scheduleDao().insert(se);
        }
    }
    
    public void clearAllData() {
        db.dayDao().deleteAll();
    }
}