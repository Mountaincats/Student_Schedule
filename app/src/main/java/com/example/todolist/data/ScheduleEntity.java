package com.example.todolist.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.time.LocalDate;

@Entity(tableName = "schedules")
public class ScheduleEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int startTime;
    public int endTime;
    public String name;
    
    @ColumnInfo(name = "note_name")
    public String noteName;
    
    @ColumnInfo(name = "note_content")
    public String noteContent;
    
    public int colorArgb;
    public boolean isTemporarySchedule;
    
    // 关联到哪一天
    public LocalDate dayDate;

    public ScheduleEntity() {}
    
    public ScheduleEntity(int startTime, int endTime, String name, String noteName, String noteContent, int colorArgb, boolean isTemporarySchedule, LocalDate dayDate) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.name = name;
        this.noteName = noteName;
        this.noteContent = noteContent;
        this.colorArgb = colorArgb;
        this.isTemporarySchedule = isTemporarySchedule;
        this.dayDate = dayDate;
    }
}