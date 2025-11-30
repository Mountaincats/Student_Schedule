package com.example.todolist.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 重复规则实现：
 * - 默认 NONE：只在起始日期出现
 * - 支持按天间隔（EVERY_N_DAYS）和按周间隔（EVERY_N_WEEKS）
 * - 截止条件支持两种（优先判断 endDate）：
 *   1. 结束日期 (endDate)：超过此日期不再重复
 *   2. 次数限制 (occurrences)：超过次数不再重复 (<=0 表示无限)
 */
public class RepeatRule {

    public enum Mode {
        NONE,
        EVERY_N_DAYS,
        EVERY_N_WEEKS
    }

    private Mode mode = Mode.NONE;
    private int interval = 1; // 间隔：天或周，>=1
    
    // 截止条件（二选一，或同时生效）
    private int occurrences = 0; // 重复次数，<=0 表示无限
    private LocalDate endDate = null; // 结束日期（包含），null 表示无日期限制
    
    private LocalDate startDate = null; // 重复起始日期；若为 null 则使用 Day 的创建日期作为起始点

    public RepeatRule() {
        this.mode = Mode.NONE;
        this.interval = 1;
        this.occurrences = 0;
        this.endDate = null;
    }

    public RepeatRule(Mode mode, int interval, int occurrences, LocalDate startDate, LocalDate endDate) {
        this.mode = mode == null ? Mode.NONE : mode;
        this.interval = Math.max(1, interval);
        this.occurrences = occurrences;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // 兼容旧构造函数
    public RepeatRule(Mode mode, int interval, int occurrences, LocalDate startDate) {
        this(mode, interval, occurrences, startDate, null);
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

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * 判断基于 dayDate（Day 的创建日期）是否在 target 出现。
     */
    public boolean occursOn(LocalDate dayDate, LocalDate target) {
        if (dayDate == null || target == null) return false;

        // 1. 确定实际起始日期
        LocalDate s = startDate == null ? dayDate : startDate;

        // 2. 基础范围检查：早于起始日期不显示
        if (target.isBefore(s)) return false;
        
        // 3. 结束日期检查：晚于结束日期不显示
        if (endDate != null && target.isAfter(endDate)) return false;

        switch (mode) {
            case NONE:
                // NONE 模式仅在起始当天显示
                return target.equals(s);
                
            case EVERY_N_DAYS: {
                long days = ChronoUnit.DAYS.between(s, target);
                // days 必须能被 interval 整除
                if (days % interval != 0) return false;
                
                // 次数限制检查
                long index = days / interval; // 第 index 次（从0开始）
                if (occurrences > 0 && index >= occurrences) return false;
                
                return true;
            }
            
            case EVERY_N_WEEKS: {
                // 必须是同一个星期几 (例如都是周一)
                if (target.getDayOfWeek() != s.getDayOfWeek()) return false;
                
                long daysDiff = ChronoUnit.DAYS.between(s, target);
                long weeks = daysDiff / 7;
                
                // 间隔检查
                if (weeks % interval != 0) return false;
                
                // 次数限制检查
                long index = weeks / interval;
                if (occurrences > 0 && index >= occurrences) return false;
                
                return true;
            }
            
            default:
                return false;
        }
    }
}