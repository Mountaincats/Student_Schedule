package com.example.todolist.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;
import com.example.todolist.R;
import com.example.todolist.adapter.DayPagerAdapter;
import com.example.todolist.data.Data;
import com.example.todolist.model.Day;
import com.example.todolist.model.RepeatRule;
import com.example.todolist.model.Schedule;
import com.example.todolist.model.Week;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ScheduleFragment extends Fragment {

    private ViewPager2 scheduleViewPager;
    private DayPagerAdapter dayPagerAdapter;
    private Week currentWeek;
    private LocalDate selectedDate;
    private TextView tvWeekTitle;
    private List<TextView> dayViews = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. 初始化 Week 数据
        if (currentWeek == null) {
            currentWeek = new Week(LocalDate.now());
            Data.getInstance().loadAllDataToWeek(currentWeek);
            selectedDate = LocalDate.now();
        }
        if (selectedDate == null) selectedDate = LocalDate.now();

        tvWeekTitle = view.findViewById(R.id.tvWeekTitle);
        updateWeekTitle();

        // 2. 初始化 ViewPager2
        scheduleViewPager = view.findViewById(R.id.scheduleViewPager);

        // 使用支持无限滑动的 Adapter
        LocalDate baseDate = currentWeek.getMonday();
        dayPagerAdapter = new DayPagerAdapter(currentWeek, baseDate);
        scheduleViewPager.setAdapter(dayPagerAdapter);

        // 设置页面间距效果
        scheduleViewPager.setPageTransformer(new MarginPageTransformer(40));

        // 监听滑动
        scheduleViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // 1. 计算新选中的日期
                LocalDate newDate = dayPagerAdapter.getDateAtPosition(position);

                // 2. 检查是否跨周
                LocalDate oldMonday = currentWeek.getMonday();
                LocalDate newMonday = newDate.with(DayOfWeek.MONDAY);

                if (!newMonday.equals(oldMonday)) {
                    // 跨周了！更新 Week 对象的锚点
                    currentWeek.setMonday(newMonday);
                    // 刷新顶部日期选择栏
                    setupDaySelector(view);
                }

                selectedDate = newDate;
                updateWeekTitle();
                updateDaySelectorHighlight();
            }
        });

        // 3. 初始化周几选择器
        setupDaySelector(view);

        // 4. 滚动到当前选中的日期
        int targetPosition = dayPagerAdapter.getPositionForDate(selectedDate);
        scheduleViewPager.setCurrentItem(targetPosition, false);

        // 5. 处理添加按钮
        FloatingActionButton fab = view.findViewById(R.id.fabAddSchedule);
        if (fab != null) {
            fab.setOnClickListener(v -> showAddScheduleDialog());
        }
    }

    private void updateWeekTitle() {
        if (tvWeekTitle != null && selectedDate != null) {
            tvWeekTitle.setText(selectedDate.format(DateTimeFormatter.ofPattern("yyyy年M月")));
        }
    }

    private void setupDaySelector(View view) {
        LinearLayout daySelectorLayout = view.findViewById(R.id.daySelectorLayout);
        if (daySelectorLayout == null) return;

        daySelectorLayout.removeAllViews();
        dayViews.clear();

        LocalDate monday = currentWeek.getMonday();
        String[] weekNames = {"一", "二", "三", "四", "五", "六", "日"};

        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            TextView dayView = new TextView(requireContext());
            final int dayIndex = i;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
            dayView.setLayoutParams(params);
            dayView.setGravity(android.view.Gravity.CENTER);
            dayView.setText(weekNames[i] + "\n" + date.getDayOfMonth());
            dayView.setTextSize(12);

            dayView.setOnClickListener(v -> {
                LocalDate targetDate = monday.plusDays(dayIndex);
                int targetPos = dayPagerAdapter.getPositionForDate(targetDate);
                scheduleViewPager.setCurrentItem(targetPos, true);
            });

            daySelectorLayout.addView(dayView);
            dayViews.add(dayView);
        }

        updateDaySelectorHighlight();
    }

    private void updateDaySelectorHighlight() {
        LocalDate monday = currentWeek.getMonday();
        long offset = ChronoUnit.DAYS.between(monday, selectedDate);

        for (int i = 0; i < dayViews.size(); i++) {
            TextView tv = dayViews.get(i);
            if (i == offset) {
                tv.setTextColor(getResources().getColor(android.R.color.white, null));
                tv.setBackgroundColor(0xFF6200EE);
            } else {
                tv.setTextColor(getResources().getColor(android.R.color.black, null));
                tv.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }
        }
    }

    private void showAddScheduleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("添加课程 (测试)");

        final EditText input = new EditText(requireContext());
        input.setHint("课程名称 (默认8:00-10:00, 每周重复)");
        builder.setView(input);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String name = input.getText().toString();
            if (!name.isEmpty()) {
                Day day = currentWeek.getDayForDate(selectedDate);
                if (day == null) {
                    RepeatRule rule = new RepeatRule(RepeatRule.Mode.EVERY_N_WEEKS, 1, 0, selectedDate);
                    day = new Day(selectedDate, false, rule);
                    currentWeek.addDay(day);
                }

                Schedule schedule = new Schedule(480, 600, name);
                day.addSchedule(schedule);

                Data.getInstance().saveDay(day);

                // 刷新 Adapter
                int currentPos = scheduleViewPager.getCurrentItem();
                dayPagerAdapter.notifyItemChanged(currentPos);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
}