package com.example.todolist.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface DayDao {
    @Query("SELECT * FROM days")
    List<DayEntity> getAllDays();

    @Insert
    long insert(DayEntity day); // 返回生成的 ID

    @Update
    void update(DayEntity day);

    @Delete
    void delete(DayEntity day);
    
    @Query("DELETE FROM days")
    void deleteAll();
    
    @Query("DELETE FROM days WHERE originDate = :date")
    void deleteDaysByOriginDate(String date);
}