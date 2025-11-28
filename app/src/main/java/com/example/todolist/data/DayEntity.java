package com.example.todolist.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.example.todolist.model.RepeatRule;
import java.time.LocalDate;

@Entity(tableName = "days")
public class DayEntity {
    @PrimaryKey(autoGenerate = true)
    public long id; // 使用 long 作为主键

    // 基础属性
    public LocalDate originDate; // 对应 Day.date
    public int activeStartHour;
    public int activeEndHour;
    public boolean isTemporaryDay;
    
    // RepeatRule 属性展平存储
    public RepeatRule.Mode repeatMode;
    public int repeatInterval;
    public int repeatOccurrences;
    public LocalDate repeatStartDate;
    public LocalDate repeatEndDate;

    public DayEntity() {}
}