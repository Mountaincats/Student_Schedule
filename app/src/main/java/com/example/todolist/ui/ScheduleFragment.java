package com.example.todolist.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;
import com.example.todolist.R;
import com.example.todolist.ScheduleEditorActivity;
import com.example.todolist.adapter.DayPagerAdapter;
import com.example.todolist.data.Data;
import com.example.todolist.model.Day;
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

    private final ActivityResultLauncher<Intent> scheduleEditorLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    refreshData();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentWeek == null) {
            currentWeek = new Week(LocalDate.now());
            Data.getInstance().loadAllDataToWeek(currentWeek);
            selectedDate = LocalDate.now();
        }
        if (selectedDate == null) selectedDate = LocalDate.now();

        tvWeekTitle = view.findViewById(R.id.tvWeekTitle);
        updateWeekTitle();

        scheduleViewPager = view.findViewById(R.id.scheduleViewPager);

        LocalDate baseDate = currentWeek.getMonday();
        dayPagerAdapter = new DayPagerAdapter(currentWeek, baseDate);
        scheduleViewPager.setAdapter(dayPagerAdapter);

        scheduleViewPager.setPageTransformer(new MarginPageTransformer(40));

        scheduleViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                LocalDate newDate = dayPagerAdapter.getDateAtPosition(position);
                LocalDate oldMonday = currentWeek.getMonday();
                LocalDate newMonday = newDate.with(DayOfWeek.MONDAY);

                if (!newMonday.equals(oldMonday)) {
                    currentWeek.setMonday(newMonday);
                    setupDaySelector(view);
                }

                selectedDate = newDate;
                updateWeekTitle();
                updateDaySelectorHighlight();
            }
        });

        setupDaySelector(view);

        int targetPosition = dayPagerAdapter.getPositionForDate(selectedDate);
        scheduleViewPager.setCurrentItem(targetPosition, false);

        FloatingActionButton fab = view.findViewById(R.id.fabAddSchedule);
        if (fab != null) {
            fab.setOnClickListener(v -> showAddScheduleActivity());
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 每次显示时刷新数据，确保从其他页面返回或编辑后数据最新
        refreshData();
    }

    private void refreshData() {
        if (currentWeek != null && dayPagerAdapter != null) {
            // 1. 记录当前状态 (周一)
            LocalDate currentMonday = currentWeek.getMonday();
            
            // 2. 创建一个新的 Week 对象，避免旧数据污染
            Week newWeek = new Week(currentMonday);
            newWeek.setMonday(currentMonday); // 确保锚点一致
            
            // 3. 从数据库重新加载所有规则数据
            Data.getInstance().loadAllDataToWeek(newWeek);
            
            // 4. 更新引用
            currentWeek = newWeek;
            
            // 5. 通知 Adapter 更新数据源
            dayPagerAdapter.updateData(currentWeek);
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

    private void showAddScheduleActivity() {
        Intent intent = new Intent(requireContext(), ScheduleEditorActivity.class);
        // 传递当前选中的日期，以便创建行程时默认选中该日期
        if (selectedDate != null) {
            intent.putExtra("targetDate", selectedDate.toString());
        }
        scheduleEditorLauncher.launch(intent);
    }
}