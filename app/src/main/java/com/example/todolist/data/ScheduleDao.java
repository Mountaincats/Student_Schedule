package com.example.todolist.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ScheduleDao {
    @Query("SELECT * FROM schedules WHERE day_id = :dayId ORDER BY startTime ASC")
    List<ScheduleEntity> getSchedulesForDayId(long dayId);

    @Insert
    void insert(ScheduleEntity schedule);

    @Update
    void update(ScheduleEntity schedule);

    @Delete
    void delete(ScheduleEntity schedule);
    
    @Query("DELETE FROM schedules WHERE day_id = :dayId")
    void deleteAllSchedulesForDayId(long dayId);
}