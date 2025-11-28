package com.example.todolist.model;

//import java.net.http.WebSocket;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 表示一周（从周一到周日），负责管理属于这周的 Day 实例并处理是否显示某个 Day（包括重复和临时替换）。
 */
public class Week {
    private LocalDate monday; // 本周周一的日期
    private int weekNumber;   // 可用于显示是第几周
    private final List<List<Day>> allDays = new ArrayList<>(7); // 7 列，从周一到周日
    // 创建一个周组，管理所有创建在本周组内的days
    
    
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

    public int getWeekNumber() {
        return weekNumber;
    }

    public List<List<Day>> getDays() {
        return Collections.unmodifiableList(allDays);
    }

    /**
     * 将某一天加入周组，具体的位置此函数会自动分配。
     */
    public boolean addDay(Day day) {
        if (day == null || day.getDate() == null) return false;
        long offset = ChronoUnit.DAYS.between(monday, day.getDate());
        if (offset < 0 || offset > 6) return false; // 不在本周
        allDays.get(day.getDayOfWeek().getValue() - 1).add(day);    // 把这一天放入对应的周几列
        day.setWeekIndex(weekNumber);
        return true;
    }

    /**
     * 在当前周的指定周几添加Day（0=周一）。如果越界返回false。
     */
    public boolean setDayAtColumn(int columnZeroBased, boolean isTemporaryDay) {    // 需要获取用户是否需要重复(isTemporaryDay)
        LocalDate date = monday.plusDays(columnZeroBased);  // 获取当前天的日期
        if (columnZeroBased < 0 || columnZeroBased > 6) return false;
        if (isTemporaryDay) {   // 临时天不重复
            RepeatRule repeatRule = new RepeatRule();
            Day day = new Day(date, isTemporaryDay, repeatRule);
            addDay(day);
        }

        //// 以下内容未完成，需要先实现监听器Listener
        // else {
        //     // 这里需要调用监听器行为获取repeatrule相关的参数
        //     RepeatRule.Mode mode = Listener.getRepeatMode;
        //     int interval = Listener.getRepeatInterval;
        //     int occurrences = Listener.getRepeatOccurrences;
        //     RepeatRule repeatRule = new RepeatRule(mode, interval, occurrences, date);
        //     Day day = new Day(monday.plusDays(columnZeroBased), isTemporaryDay, repeatRule);
        //     addDay(day);
        // }
        return true;
    }

    /**
     * 根据日期返回在本周对应的 Day（如果存在），否则返回 null。
     * 该方法会优先返回已放入 days 列表的 Day；如果该列为空则返回 null（上层可通过重复规则合并）。
     */
    public Day getDayForDate(LocalDate date) {
        if (date == null) return null;
        long offset = ChronoUnit.DAYS.between(monday, date);
        if (offset < 0 || offset > 6) return null;
        List<Day> column = allDays.get((int) offset);
        return column.isEmpty() ? null : column.get(0);
    }

    /**
     * 返回显示在当前周中指定列（0=周一）的 Day；如果该列为空但有重复的 Day（由外部传入），应该由上层合并逻辑决定。
     */
    public Day getDayAtColumn(int columnZeroBased){
        if(columnZeroBased < 0 || columnZeroBased > 6) return null;
        List<Day> column = allDays.get(columnZeroBased);
        return column.isEmpty() ? null : column.get(0);
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