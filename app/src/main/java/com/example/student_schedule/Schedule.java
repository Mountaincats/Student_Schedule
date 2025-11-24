package com.example.student_schedule;

import java.util.Objects;

public class Schedule {
    private int startTime;  // 从0点开始，每一分钟为单位，比如：12:00 = 720，00:01 = 1
    private int endTime;
    private String name;    // 名称
    private Note note;
    private int colorArgb;  // 颜色，使用 ARGB 整数表示
    private boolean isTemporarySchedule; // 是否为临时行程（临时行程不会加入天的重复，并且在显示时有更高的优先级，比如说，两个行程重叠，优先显示临时行程）

    public Schedule(int startTime, int endTime, String name, Note note, int colorArgb, boolean isTemporarySchedule){
        validateTimes(startTime, endTime);
        this.startTime = startTime;
        this.endTime = endTime;
        this.name = name;
        this.note = note == null ? new Note() : note;
        this.colorArgb = colorArgb;
        this.isTemporarySchedule = isTemporarySchedule;
    }

    public Schedule(int startTime, int endTime, String name){
        this(startTime, endTime, name, new Note(), 0xFF2196F3, false); // 默认颜色蓝色
    }

    public static class Note {
        private String name = null;
        private String content = null;

        Note(){
            this.name = "备注";
            this.content = "";
        }

        Note(String name , String content){
            this.name = name;
            this.content = content;
        }

        Note(String content){
            this.name = "备注";
            this.content = content;
        }

        public String getName() {
            return name;
        }

        public String getContent() {
            return content;
        }

        @Override
        public String toString(){
            return name + ":" + "\n" + content;
        }
    }

    private void validateTimes(int start, int end) {
        final int MIN = 0;
        final int MAX = 24 * 60; // 不含 1440
        if (start < MIN || start > MAX || end < MIN || end > MAX) {
            throw new IllegalArgumentException("startTime/endTime 必须在 0..1440 之间（分钟）");
        }
        if (start >= end) {
            throw new IllegalArgumentException("startTime 必须小于 endTime");
        }
    }

    public void setStartTime(int startTime) {
        validateTimes(startTime, this.endTime);
        this.startTime = startTime;
    }

    public void setEndTime(int endTime) {
        validateTimes(this.startTime, endTime);
        this.endTime = endTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNote(Note note) {
        this.note = note == null ? new Note() : note;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public String getName() {
        return name;
    }

    public Note getNote() {
        if (note == null) note = new Note();
        return note;
    }

    public int getColorArgb() {
        return colorArgb;
    }

    public boolean isTemporarySchedule() {
        return isTemporarySchedule;
    }

    public void setColorArgb(int colorArgb) {
        this.colorArgb = colorArgb;
    }

    public void setTemporarySchedule(boolean temporarySchedule) {
        this.isTemporarySchedule = temporarySchedule;
    }

    @Override
    public String toString() {
        return (name == null ? "" : name + " ") + formatTime(startTime) + "-" + formatTime(endTime) + (isTemporarySchedule ? " (临时)" : "");
    }

    private static String formatTime(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return String.format("%02d:%02d", h, m);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schedule schedule = (Schedule) o;
        return startTime == schedule.startTime &&
                endTime == schedule.endTime &&
                Objects.equals(name, schedule.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, name);
    }
}

