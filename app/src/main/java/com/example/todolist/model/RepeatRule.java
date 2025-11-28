package com.example.todolist.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 简单的重复规则实现（可扩展）：
 * - 默认 NONE：只在起始日期出现
 * - 支持按天间隔（EVERY_N_DAYS）和按周间隔（EVERY_N_WEEKS）
 * - 支持重复次数限制（occurrences <= 0 表示无限）
 * - 支持自定义起始日期（若 null 则使用 Day 创建日期作为起始）
 */
public class RepeatRule {

    public enum Mode {
        NONE,
        EVERY_N_DAYS,
        EVERY_N_WEEKS
    }

    private Mode mode = Mode.NONE;
    private int interval = 1; // 间隔：天或周，>=1
    private int occurrences = 0; // 重复次数，<=0 表示无限
    private LocalDate startDate = null; // 重复起始日期；若为 null 则使用 Day 的创建日期作为起始点

    public RepeatRule() {
        this.mode = Mode.NONE;
        this.interval = 1;
        this.occurrences = 0;
    }

    public RepeatRule(Mode mode, int interval, int occurrences, LocalDate startDate) {
        this.mode = mode == null ? Mode.NONE : mode;
        this.interval = Math.max(1, interval);
        this.occurrences = occurrences;
        this.startDate = startDate;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode == null ? Mode.NONE : mode;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = Math.max(1, interval);
    }

    public int getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * 判断基于 dayDate（Day 的创建日期）是否在 target 出现。
     */
    public boolean occursOn(LocalDate dayDate, LocalDate target) {
        if (dayDate == null || target == null) return false;

        LocalDate s = startDate == null ? dayDate : startDate;
        if (target.isBefore(s)) return false;

        switch (mode) {
            case NONE:
                return target.equals(dayDate);
            case EVERY_N_DAYS: {
                long days = ChronoUnit.DAYS.between(s, target);
                if (days < 0) return false;
                if (days % interval != 0) return false;
                long index = days / interval; // 第 index 次（从0开始）
                return occurrences <= 0 || index < occurrences;
            }
            case EVERY_N_WEEKS: {
                // 只有当 weekday 与起始日相同且周间隔满足时才出现
                if (target.getDayOfWeek() != s.getDayOfWeek()) return false;
                long weeks = ChronoUnit.WEEKS.between(s, target);
                if (weeks < 0) return false;
                if (weeks % interval != 0) return false;
                long index = weeks / interval;
                return occurrences <= 0 || index < occurrences;
            }
            default:
                return false;
        }
    }
}