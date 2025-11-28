package com.example.todolist.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;
import java.util.Objects;

/**
 * 表示一天：包含日期、周号、周几、活动时段、当天的行程列表、重复规则等。
 */
public class Day {
    private LocalDate date;         // 创建天的日期（也可作为默认重复起始点）
    private int weekIndex;          // 所在周索引（可由外部 Week 管理）
    private DayOfWeek dayOfWeek;

    private int activeStartHour = 8;   // 默认 8 点，单位小时，范围 [0,24]
    private int activeEndHour = 22;    // 默认 22 点

    private RepeatRule repeatRule = new RepeatRule(); // 默认不重复
    private boolean isTemporaryDay = false; // 临时的天会暂时覆盖对应的重复逻辑（只在该天生效的日期有效），注意，这个变量只控制是否有覆盖优先性，具体的重复性由RepeatRule控制
    
    private final List<Schedule> schedules = new ArrayList<>();

    public Day(LocalDate date){
        if (date == null) {
            throw new IllegalArgumentException("date 不能为 null");
        }
        this.date = date;
        this.dayOfWeek = date.getDayOfWeek();
    }

    public Day(LocalDate date, boolean istemporaryDay, RepeatRule repeatRule){
        if (date == null) {
            throw new IllegalArgumentException("date 不能为 null");
        }
        this.date = date;
        this.dayOfWeek = date.getDayOfWeek();
        this.isTemporaryDay = istemporaryDay;
        this.repeatRule = repeatRule;
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
        // 按开始时间排序，保证当天行程按时间先后排列
        schedules.sort(Comparator.comparingInt(Schedule::getStartTime));
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
        return isTemporaryDay;
    }

    public void setTemporaryDay(boolean temporaryDay) {
        this.isTemporaryDay = temporaryDay;
    }

    /**
     * 判断这个 Day 是否应在目标日期上出现。
     * 逻辑：
     *  - 如果 temporaryDay 且 date.equals(target) -> 出现（仅本日）
     *  - 否则依据 repeatRule 判断（repeatRule 默认表示只出现创建日期这一天）
     */
    public boolean appearsOn(LocalDate target) {
        if (target == null) return false;
        if (isTemporaryDay && date.equals(target)) return true;
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