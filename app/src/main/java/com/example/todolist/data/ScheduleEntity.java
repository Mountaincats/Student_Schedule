package com.example.todolist.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "schedules",
        foreignKeys = @ForeignKey(entity = DayEntity.class,
                                  parentColumns = "id",
                                  childColumns = "day_id",
                                  onDelete = CASCADE),
        indices = {@Index("day_id")})
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
    
    @ColumnInfo(name = "day_id")
    public long dayId; // 外键关联到 DayEntity.id

    public ScheduleEntity() {}
    
    public ScheduleEntity(int startTime, int endTime, String name, String noteName, String noteContent, int colorArgb, boolean isTemporarySchedule, long dayId) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.name = name;
        this.noteName = noteName;
        this.noteContent = noteContent;
        this.colorArgb = colorArgb;
        this.isTemporarySchedule = isTemporarySchedule;
        this.dayId = dayId;
    }
}