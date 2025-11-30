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

    /**
     * 加载所有数据到 Week 中。
     */
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

    /**
     * 保存某一个 Day（包含它的 Schedules）。
     * 采用覆盖策略：先删除该日期下的旧规则，再插入新的。
     */
    public void saveDay(Day day) {
        if (day == null) return;
        
        // 1. 先删除数据库中已有的针对该日期的规则
        // 注意：这要求你的 DayDao 已经实现了 deleteDaysByOriginDate 方法
        // 并且 Converters 能够正确处理 LocalDate 的转换
        db.dayDao().deleteDaysByOriginDate(day.getDate().toString());
        
        // 2. 准备新的 DayEntity
        DayEntity entity = new DayEntity();
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

        // 3. 插入新的 DayEntity 并获取 ID
        long dayId = db.dayDao().insert(entity); 

        // 4. 保存 Schedules
        for (Schedule schedule : day.getSchedules()) {
            ScheduleEntity se = new ScheduleEntity(
                    schedule.getStartTime(),
                    schedule.getEndTime(),
                    schedule.getName(),
                    schedule.getNote().getName(),
                    schedule.getNote().getContent(),
                    schedule.getColorArgb(),
                    schedule.isTemporarySchedule(),
                    dayId // 关联刚刚生成的 Day ID
            );
            db.scheduleDao().insert(se);
        }
    }
    
    /**
     * 清空所有数据（调试用）
     */
    public void clearAllData() {
        db.dayDao().deleteAll(); 
    }
}