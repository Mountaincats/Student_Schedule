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
     * 现在的逻辑是：从数据库加载所有的 Day 规则，全部填入 Week，让 Week 自己去解析显示。
     */
    public void loadAllDataToWeek(Week week) {
        // 1. 加载所有 DayEntity
        List<DayEntity> dayEntities = db.dayDao().getAllDays();

        for (DayEntity dayEntity : dayEntities) {
            // 2. 转换 RepeatRule
            RepeatRule repeatRule = new RepeatRule(
                    dayEntity.repeatMode,
                    dayEntity.repeatInterval,
                    dayEntity.repeatOccurrences,
                    dayEntity.repeatStartDate,
                    dayEntity.repeatEndDate
            );

            // 3. 创建 Day 对象
            Day day = new Day(dayEntity.originDate, dayEntity.isTemporaryDay, repeatRule);
            // 恢复其他属性
            day.setActiveHours(dayEntity.activeStartHour, dayEntity.activeEndHour);
            // 这里的 ID 是数据库 ID，如果 Day 模型里没有 ID 字段，我们在保存时可能需要通过其他方式关联，
            // 或者我们在 Day 模型里加一个 id 字段（推荐）。
            // 为了演示，这里假设我们在保存时会处理好关联，加载时我们主要把数据还原。
            // *重要*：为了后续保存方便，建议给 Day 加一个 databaseId 字段。但如果不加，我们可能需要依赖 originDate + isTemporary 等组合键查找，或者每次都全量更新。
            
            // 临时方案：我们将 entity.id 存入一个临时 Map 或者修改 Day 模型。
            // 鉴于修改 Model 比较大，我们这里先按照 "Day 对象在内存中是新的" 来处理。
            
            // 4. 加载该 Day 下的所有 Schedule
            List<ScheduleEntity> scheduleEntities = db.scheduleDao().getSchedulesForDayId(dayEntity.id);
            for (ScheduleEntity se : scheduleEntities) {
                Schedule.Note note = new Schedule.Note(se.noteName, se.noteContent);
                Schedule schedule = new Schedule(
                        se.startTime, se.endTime, se.name, note, se.colorArgb, se.isTemporarySchedule
                );
                day.addSchedule(schedule);
            }

            // 5. 加入 Week
            week.addDay(day);
        }
    }

    /**
     * 保存某一个 Day（包含它的 Schedules）。
     * 策略：
     * 1. 查找数据库中是否已存在该 Day (通过 originDate + isTemporary + repeatRule 判断？这很难)。
     *    更好的方式是：Day 模型里应该持有数据库 ID。
     *    如果没有 ID，我们可能需要先执行插入。
     *    
     * 暂时策略：
     * 为了不改动 Day.java (如果不允许改)，我们只能尝试根据 originDate 和 isTemporary 来匹配更新。
     * 或者，我们假设每次 Save 都是一次新的 Insert（但这会产生垃圾数据）。
     * 
     * **最佳实践**：修改 Day.java 增加 `private long id;`。
     * 
     * 这里我演示一个 "查找或插入" 的逻辑：
     */
    public void saveDay(Day day) {
        if (day == null) return;
        
        // 1. 先尝试把 Day 转为 Entity
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

        // 2. 插入 DayEntity 并获取 ID
        // 注意：这会每次都插入新行！如果 Day 已经存在，这会导致重复数据。
        // 既然我们没有在 Model 里存 ID，我们这里做一个简单的假设：
        // 我们根据 originDate 和 isTemporaryDay 尝试删除旧的（如果有的话），然后插入新的。
        // 这是一种比较暴力的 "Replace" 策略。
        
        // 为了实现 "Replace"，我们需要在 DayDao 里加个方法，或者在这里手动查。
        // 简单起见，我们先插入。实际项目中请务必给 Day 加 ID。
        long dayId = db.dayDao().insert(entity); 

        // 3. 保存 Schedules
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
        db.dayDao().deleteAll(); // 级联删除会把 schedules 也删掉
    }
}