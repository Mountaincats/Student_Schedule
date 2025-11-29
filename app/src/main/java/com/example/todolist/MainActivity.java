package com.example.todolist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;
import com.example.todolist.adapter.DailyTaskAdapter;
import com.example.todolist.adapter.DayPagerAdapter;
import com.example.todolist.data.DailyTaskManager;
import com.example.todolist.data.Data;
import com.example.todolist.model.DailyTask;
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

public class MainActivity extends AppCompatActivity implements DailyTaskAdapter.OnTaskClickListener {
    private ImageButton btnSettings;
    private Button btnSchedule, btnTodo, btnDaily;
    private FrameLayout contentFrame;
    private Toolbar toolbar;

    // 每日任务相关
    private RecyclerView dailyTasksRecyclerView;
    private DailyTaskAdapter dailyTaskAdapter;
    private DailyTaskManager dailyTaskManager;
    private List<DailyTask> dailyTaskList;

    // 日程表相关
    private ViewPager2 scheduleViewPager;
    private DayPagerAdapter dayPagerAdapter;
    private Week currentWeek;
    private LocalDate selectedDate;
    private TextView tvWeekTitle;
    private List<TextView> dayViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 初始化数据库
        Data.init(getApplicationContext());

        // 初始化每日任务
        dailyTaskManager = new DailyTaskManager(this);
        dailyTaskList = dailyTaskManager.getDailyTasks();

        initViews();
        setupClickListeners();

        // 默认显示课表界面
        showScheduleView();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnSettings = findViewById(R.id.btnSettings);
        btnSchedule = findViewById(R.id.btnSchedule);
        btnTodo = findViewById(R.id.btnTodo);
        btnDaily = findViewById(R.id.btnDaily);
        contentFrame = findViewById(R.id.contentFrame);
    }

    private void setupClickListeners() {
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        btnSchedule.setOnClickListener(v -> showScheduleView());
        btnTodo.setOnClickListener(v -> showTodoView());
        btnDaily.setOnClickListener(v -> showDailyView());
    }

    // ================= 日程表逻辑 (Schedule) =================

    private void showScheduleView() {
        contentFrame.removeAllViews();
        View scheduleView = getLayoutInflater().inflate(R.layout.layout_schedule, contentFrame, false);
        contentFrame.addView(scheduleView);
        updateTabStates(btnSchedule);

        // 1. 初始化 Week 数据
        if (currentWeek == null) {
            currentWeek = new Week(LocalDate.now()); 
            Data.getInstance().loadAllDataToWeek(currentWeek);
            selectedDate = LocalDate.now(); 
        }
        if (selectedDate == null) selectedDate = LocalDate.now();

        tvWeekTitle = scheduleView.findViewById(R.id.tvWeekTitle);
        updateWeekTitle();

        // 2. 初始化 ViewPager2
        scheduleViewPager = scheduleView.findViewById(R.id.scheduleViewPager);
        
        // 使用支持无限滑动的 Adapter
        // 我们以 currentWeek 的周一作为基准日期
        LocalDate baseDate = currentWeek.getMonday();
        dayPagerAdapter = new DayPagerAdapter(currentWeek, baseDate);
        scheduleViewPager.setAdapter(dayPagerAdapter);
        
        // 设置页面间距效果 (卡片式滑动)
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
                    setupDaySelector(scheduleView); // 重新生成周一到周日
                }
                
                selectedDate = newDate;
                updateWeekTitle();
                updateDaySelectorHighlight();
            }
        });

        // 3. 初始化周几选择器
        setupDaySelector(scheduleView);
        
        // 4. 滚动到当前选中的日期
        // 计算相对于 Adapter 基准日期的偏移量
        int targetPosition = dayPagerAdapter.getPositionForDate(selectedDate);
        scheduleViewPager.setCurrentItem(targetPosition, false);

        // 5. 处理添加按钮
        FloatingActionButton fab = scheduleView.findViewById(R.id.fabAddSchedule);
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
            TextView dayView = new TextView(this);
            final int dayIndex = i;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
            dayView.setLayoutParams(params);
            dayView.setGravity(android.view.Gravity.CENTER);
            dayView.setText(weekNames[i] + "\n" + date.getDayOfMonth());
            dayView.setTextSize(12);

            // 点击事件：计算目标日期的 position 并跳转
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
        // 计算当前选中日期相对于本周一的偏移 (0-6)
        long offset = ChronoUnit.DAYS.between(monday, selectedDate);
        
        for (int i = 0; i < dayViews.size(); i++) {
            TextView tv = dayViews.get(i);
            if (i == offset) {
                tv.setTextColor(getColor(android.R.color.white));
                tv.setBackgroundColor(0xFF6200EE); 
            } else {
                tv.setTextColor(getColor(android.R.color.black));
                tv.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }
        }
    }

    private void showAddScheduleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加课程 (测试)");

        final EditText input = new EditText(this);
        input.setHint("课程名称 (默认8:00-10:00, 每周重复)");
        builder.setView(input);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String name = input.getText().toString();
            if (!name.isEmpty()) {
                // 1. 无论 selectedDate 是哪天，Week.getDayForDate 都能找到对应的规则 (因为解耦了)
                Day day = currentWeek.getDayForDate(selectedDate);
                if (day == null) {
                    RepeatRule rule = new RepeatRule(RepeatRule.Mode.EVERY_N_WEEKS, 1, 0, selectedDate);
                    day = new Day(selectedDate, false, rule);
                    currentWeek.addDay(day);
                }
                
                Schedule schedule = new Schedule(480, 600, name);
                day.addSchedule(schedule);
                
                Data.getInstance().saveDay(day);
                
                // 刷新 Adapter：简单通知一下数据变更
                // 由于 Adapter 直接持有 Week 引用，我们只需要 notifyItemChanged
                int currentPos = scheduleViewPager.getCurrentItem();
                dayPagerAdapter.notifyItemChanged(currentPos);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    // ================= 其他 Tab 逻辑 =================

    private void showTodoView() {
        contentFrame.removeAllViews();
        View todoView = getLayoutInflater().inflate(R.layout.layout_todo, contentFrame, false);
        contentFrame.addView(todoView);
        updateTabStates(btnTodo);
    }

    private void showDailyView() {
        contentFrame.removeAllViews();
        View dailyView = getLayoutInflater().inflate(R.layout.layout_daily, contentFrame, false);
        contentFrame.addView(dailyView);
        updateTabStates(btnDaily);
        initDailyRecyclerView(dailyView);
    }

    private void initDailyRecyclerView(View dailyView) {
        dailyTasksRecyclerView = dailyView.findViewById(R.id.dailyTasksRecyclerView);
        dailyTaskAdapter = new DailyTaskAdapter(dailyTaskList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        dailyTasksRecyclerView.setLayoutManager(layoutManager);
        dailyTasksRecyclerView.setAdapter(dailyTaskAdapter);
    }

    private void updateTabStates(Button activeTab) {
        Button[] tabs = {btnSchedule, btnTodo, btnDaily};
        for (Button tab : tabs) {
            tab.setSelected(tab == activeTab);
        }
    }

    // DailyTaskAdapter 接口实现
    @Override
    public void onAddTaskClick() {
        showAddTaskDialog();
    }

    @Override
    public void onTaskCompleteClick(DailyTask task, boolean completed) {
        dailyTaskManager.markTaskCompleted(task, completed);
        dailyTaskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskDeleteClick(DailyTask task) {
        showDeleteConfirmationDialog(task);
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加每日任务");
        final EditText input = new EditText(this);
        input.setHint("请输入任务内容");
        builder.setView(input);
        builder.setPositiveButton("添加", (dialog, which) -> {
            String taskContent = input.getText().toString().trim();
            if (!taskContent.isEmpty()) {
                int newTaskId = dailyTaskManager.getNextTaskId();
                DailyTask newTask = new DailyTask(newTaskId, taskContent);
                dailyTaskManager.addTask(newTask);
                dailyTaskAdapter.notifyItemInserted(0);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showDeleteConfirmationDialog(final DailyTask task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认删除");
        builder.setMessage("确定要删除这个任务吗？");
        builder.setPositiveButton("删除", (dialog, which) -> {
            int position = dailyTaskList.indexOf(task);
            if (position != -1) {
                dailyTaskManager.deleteTask(task);
                dailyTaskAdapter.notifyItemRemoved(position);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dailyTaskManager != null) {
            dailyTaskManager.saveData();
        }
    }
}