package com.example.todolist.model;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 表示一周的“排课方案”或“视图”。
 * <p>
 * 它维护了一个二维列表（7列 x N行），每一列包含了该“星期几”所有可能的 Day 候选者（规则）。
 * 当查询具体的日期时，它会根据 appearsOn 和优先级动态计算出应该显示哪一个 Day。
 */
public class Week {
    private LocalDate monday; // 本周周一的日期（锚点，用于确定查询的具体日期范围）
    private int weekNumber;   // 可用于显示是第几周
    private final List<List<Day>> allDays = new ArrayList<>(7); // 7 列，从周一到周日。每一列可能包含多个 Day 规则。

    public Week(LocalDate anyDateInWeek){
        if (anyDateInWeek == null) {
            throw new IllegalArgumentException("anyDateInWeek 不能为 null");
        }
        this.monday = anyDateInWeek.with(DayOfWeek.MONDAY);
        this.weekNumber = 1;
        this.allDays.clear();
        for (int i = 0; i < 7; i++) {
            allDays.add(new ArrayList<>());
        }
    }

    public LocalDate getMonday() {
        return monday;
    }
    
    /**
     * 设置当前周的起始日期（周一）。
     * 设置后，getEffectiveDays() 返回的新列表将对应这一周。
     * 这允许在 ViewPager 切换时复用同一个 Week 对象来渲染不同周的数据。
     */
    public void setMonday(LocalDate monday) {
        if (monday == null) {
            throw new IllegalArgumentException("monday cannot be null");
        }
        this.monday = monday.with(DayOfWeek.MONDAY);
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public List<List<Day>> getDays() {
        return Collections.unmodifiableList(allDays);
    }
    
    /**
     * 获取当前锚点周（this.monday 所在周）的所有有效 Day 列表 (7个)。
     */
    public List<Day> getEffectiveDays() {
        return getEffectiveDays(this.monday);
    }

    /**
     * 获取指定周（由 startOfWeek 决定）的所有有效 Day 列表。
     * 哪怕 startOfWeek 不是本周，此方法也能正确根据规则返回那一周应该显示的课程。
     */
    public List<Day> getEffectiveDays(LocalDate startOfWeek) {
        if (startOfWeek == null) return new ArrayList<>();
        LocalDate start = startOfWeek.with(DayOfWeek.MONDAY);
        
        List<Day> result = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            Day effectiveDay = getDayForDate(date);
            
            // 如果当天没有匹配的 Day，可以选择返回 null 或者一个空的 Day (取决于上层 UI 怎么处理)
            result.add(effectiveDay);
        }
        return result;
    }

    /**
     * 将某一天（规则）加入周组。
     * <p>
     * 改进：不再限制 day 的日期必须在本周内。只要它是“周一”的 Day，就放入周一的候选池。
     * 这样可以支持添加“过去的重复规则”。
     */
    public boolean addDay(Day day) {
        if (day == null || day.getDate() == null) return false;
        
        // 直接根据 Day 的星期几，放入对应的列
        int colIndex = day.getDayOfWeek().getValue() - 1; // 1=Mon -> 0
        allDays.get(colIndex).add(day);
        
        // day.setWeekIndex(weekNumber); // 这一行可能需要斟酌，如果 Day 是跨周重复的，绑定特定的 weekIndex 可能会有歧义
        return true;
    }

    /**
     * 在当前周的指定周几创建并添加一个新的 Day。
     */
    public boolean setDayAtColumn(int columnZeroBased, boolean isTemporaryDay) {
        if (columnZeroBased < 0 || columnZeroBased > 6) return false;
        
        // 计算出这一列在本周对应的具体日期
        LocalDate date = monday.plusDays(columnZeroBased);
        
        if (isTemporaryDay) {
            RepeatRule repeatRule = new RepeatRule(); // 临时天通常不重复
            Day day = new Day(date, isTemporaryDay, repeatRule);
            addDay(day);
        }
        // TODO: 处理非临时天的逻辑 (Listener etc.)
        return true;
    }

    /**
     * 核心逻辑：根据具体日期，从候选列表中选出最应该显示的 Day。
     * <p>
     * 改进：现在支持查询任意日期，不再局限于本周 (this.monday)。
     * 它会根据日期的星期几，去对应的规则列查找，并判断 appearsOn。
     */
    public Day getDayForDate(LocalDate date) {
        if (date == null) return null;
        
        // 使用 DayOfWeek 计算列索引 (Monday=1 -> 0, Sunday=7 -> 6)
        int colIndex = date.getDayOfWeek().getValue() - 1;
        
        List<Day> candidates = allDays.get(colIndex);
        if (candidates.isEmpty()) return null;

        Day bestMatch = null;

        for (Day day : candidates) {
            if (day.appearsOn(date)) {
                // 如果还没找到匹配项，直接暂定为这个
                if (bestMatch == null) {
                    bestMatch = day;
                } else {
                    // 仲裁逻辑：临时天 > 普通重复天
                    if (day.isTemporaryDay() && !bestMatch.isTemporaryDay()) {
                        bestMatch = day;
                    }
                    // 如果都是临时天，或者都是重复天，可能需要根据创建时间或其他逻辑判断
                }
            }
        }
        return bestMatch;
    }

    /**
     * 获取本周第 [column] 列应该显示的 Day。
     */
    public Day getDayAtColumn(int columnZeroBased){
        if(columnZeroBased < 0 || columnZeroBased > 6) return null;
        LocalDate targetDate = monday.plusDays(columnZeroBased);
        return getDayForDate(targetDate);
    }

    /**
     * 辅助方法：判断某个具体的 Day 规则是否在本周生效。
     */
    public LocalDate appearsInWeek(Day day) {
        if (day == null) return null;
        // 优化：只需要检查它对应的星期几那一天即可，不需要遍历7天
        int col = day.getDayOfWeek().getValue() - 1;
        LocalDate targetDate = monday.plusDays(col);
        
        if (day.appearsOn(targetDate)) {
            return targetDate;
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(monday);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Week week = (Week) o;
        return Objects.equals(monday, week.monday);
    }
}