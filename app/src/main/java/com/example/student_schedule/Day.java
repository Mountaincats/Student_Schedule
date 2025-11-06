package com.example.student_schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import com.example.student_schedule.RepeatRule;

/**
 * 表示一天：包含日期、周号、周几、活动时段、当天的行程列表、重复规则等。
 */
public class Day {
    private LocalDate date;         // 创建天的日期（也可作为默认重复起始点）
    private int weekIndex;          // 所在周索引（可由外部 Week 管理）
    private DayOfWeek dayOfWeek;

    private int activeStartHour = 8;   // 默认 8 点，单位小时，范围 [0,24]
    private int activeEndHour = 22;    // 默认 22 点

    private final List<Schedule> schedules = new ArrayList<>();

    // 重复相关
    private RepeatRule repeatRule = new RepeatRule(); // 默认不重复
    private boolean temporaryDay = false; // 临时的天会暂时覆盖对应的重复逻辑（只在特定 date 有效）

    public Day(LocalDate date){
        if (date == null) {
            throw new IllegalArgumentException("date 不能为 null");
        }
        this.date = date;
        this.dayOfWeek = date.getDayOfWeek();
    }

    public LocalDate getDate() {
        return date;
    }

    public int getWeekIndex() {
        return weekIndex;
    }

    public void setWeekIndex(int weekIndex) {
        this.weekIndex = weekIndex;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public int getActiveStartHour() {
        return activeStartHour;
    }

    public int getActiveEndHour() {
        return activeEndHour;
    }

    /**
     * 设置活动时间（小时）。限制在 0..24 范围，且 start < end。
     */
    public boolean setActiveHours(int startHour, int endHour){
        if(startHour < 0 || endHour > 24 || startHour >= endHour) return false;
        this.activeStartHour = startHour;
        this.activeEndHour = endHour;
        return true;
    }

    public List<Schedule> getSchedules() {
        return Collections.unmodifiableList(schedules);
    }

    public void addSchedule(Schedule s){
        if(s == null) return;
        schedules.add(s);
    }

    public boolean removeSchedule(Schedule s){
        return schedules.remove(s);
    }

    public RepeatRule getRepeatRule() {
        return repeatRule;
    }

    public void setRepeatRule(RepeatRule repeatRule) {
        this.repeatRule = repeatRule == null ? new RepeatRule() : repeatRule;
    }

    public boolean isTemporaryDay() {
        return temporaryDay;
    }

    public void setTemporaryDay(boolean temporaryDay) {
        this.temporaryDay = temporaryDay;
    }

    /**
     * 判断这个 Day 是否应在目标日期上出现。
     * 逻辑：
     *  - 如果 temporaryDay 且 date.equals(target) -> 出现（仅本日）
     *  - 否则依据 repeatRule 判断（repeatRule 默认表示只出现创建日期这一天）
     */
    public boolean appearsOn(LocalDate target) {
        if (target == null) return false;
        if (temporaryDay && date.equals(target)) return true;
        return repeatRule.occursOn(date, target);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Day day = (Day) o;
        return Objects.equals(date, day.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}