package com.example.student_schedule;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 表示一周（从周一到周日），负责管理属于这周的 Day 实例并处理是否显示某个 Day（包括重复和临时替换）。
 */
public class Week {
    private LocalDate monday; // 本周周一的日期
    private int weekNumber;   // 可用于显示是第几周
    private final List<Day> days = new ArrayList<>(7); // 7 列，从周一到周日

    public Week(LocalDate anyDateInWeek, int weekNumber){
        this.monday = anyDateInWeek.with(DayOfWeek.MONDAY);
        this.weekNumber = weekNumber;
        // 初始化占位 Day（可后续替换或向中添加行程）
        for(int i=0;i<7;i++){
            days.add(null);
        }
    }

    public LocalDate getMonday() {
        return monday;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public List<Day> getDays() {
        return Collections.unmodifiableList(days);
    }

    /**
     * 将 Day 放入对应列（如果 day.date 落在本周）。
     */
    public boolean putDay(Day day) {
        if (day == null) return false;
        int offset = (int) (day.getDate().getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue()); // 0..6
        LocalDate expected = monday.plusDays(offset);
        if (!day.getDate().equals(expected)) return false;
        days.set(offset, day);
        day.setWeekIndex(weekNumber);
        return true;
    }

    /**
     * 返回显示在当前周中指定列（0=周一）的 Day；如果该列为空但有重复的 Day（由外部传入），应该由上层合并逻辑决定。
     */
    public Day getDayAtColumn(int columnZeroBased){
        if(columnZeroBased < 0 || columnZeroBased > 6) return null;
        return days.get(columnZeroBased);
    }

    /**
     * 判断给定的 Day（可能来自重复规则或创建的 Day）是否应该出现在本周内某日，
     * 并返回对应的日期（若不出现则返回 null）。
     *
     * 该方法不会修改内部 days 列表，只用于判断。
     */
    public LocalDate appearsInWeek(Day day) {
        if (day == null) return null;
        for (int i = 0; i < 7; i++) {
            LocalDate candidate = monday.plusDays(i);
            if (day.appearsOn(candidate)) return candidate;
        }
        return null;
    }
}