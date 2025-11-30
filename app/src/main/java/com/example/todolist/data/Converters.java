package com.example.todolist.data;

import androidx.room.TypeConverter;
import com.example.todolist.model.RepeatRule;
import java.time.LocalDate;

public class Converters {
    @TypeConverter
    public static LocalDate fromTimestamp(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    @TypeConverter
    public static String dateToTimestamp(LocalDate date) {
        return date == null ? null : date.toString();
    }

    @TypeConverter
    public static RepeatRule.Mode fromModeString(String value) {
        return value == null ? RepeatRule.Mode.NONE : RepeatRule.Mode.valueOf(value);
    }

    @TypeConverter
    public static String modeToString(RepeatRule.Mode mode) {
        return mode == null ? RepeatRule.Mode.NONE.name() : mode.name();
    }
}