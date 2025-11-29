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
import com.example.todolist.adapter.TodoAdapter;
import com.example.todolist.data.DailyTaskManager;
import com.example.todolist.data.Data;
import com.example.todolist.data.TodoManager;
import com.example.todolist.model.DailyTask;
import com.example.todolist.model.Day;
import com.example.todolist.model.RepeatRule;
import com.example.todolist.model.Schedule;
import com.example.todolist.model.TodoTask;
import com.example.todolist.model.Week;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DailyTaskAdapter.OnTaskClickListener, TodoAdapter.OnTodoTaskClickListener {
    private ImageButton btnSettings;
    private Button btnSchedule, btnTodo, btnDaily;
    private FrameLayout contentFrame;
    private Toolbar toolbar;

    // 每日任务相关
    private RecyclerView dailyTasksRecyclerView;
    private DailyTaskAdapter dailyTaskAdapter;
    private DailyTaskManager dailyTaskManager;
    private List<DailyTask> dailyTaskList;

    // 待办事项相关变量
    private RecyclerView todoTasksRecyclerView;
    private TodoAdapter todoAdapter;
    private TodoManager todoManager;
    private List<TodoTask> todoTaskList;

    // 日程表相关 (新增恢复)
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

        // 1. 初始化数据库 (恢复丢失的逻辑)
        Data.init(getApplicationContext());

        // 初始化数据管理器
        dailyTaskManager = new DailyTaskManager(this);
        dailyTaskList = dailyTaskManager.getDailyTasks();

        // 初始化待办事项数据管理器
        todoManager = new TodoManager(this);
        todoTaskList = todoManager.getTodoTasks();

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
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        btnSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showScheduleView();
            }
        });

        btnTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTodoView();
            }
        });

        btnDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDailyView();
            }
        });
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
                    setupDaySelector(scheduleView); 
                }
                
                selectedDate = newDate;
                updateWeekTitle();
                updateDaySelectorHighlight();
            }
        });

        // 3. 初始化周几选择器
        setupDaySelector(scheduleView);
        
        // 4. 滚动到当前选中的日期
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

    // ================= 其他 Tab 逻辑 =================

    private void showTodoView() {
        contentFrame.removeAllViews();
        View todoView = getLayoutInflater().inflate(R.layout.layout_todo, contentFrame, false);
        contentFrame.addView(todoView);
        updateTabStates(btnTodo);

        // 初始化待办事项RecyclerView
        initTodoRecyclerView(todoView);
    }

    private void showDailyView() {
        contentFrame.removeAllViews();
        View dailyView = getLayoutInflater().inflate(R.layout.layout_daily, contentFrame, false);
        contentFrame.addView(dailyView);
        updateTabStates(btnDaily);

        // 初始化每日任务RecyclerView
        initDailyRecyclerView(dailyView);
    }

    private void initDailyRecyclerView(View dailyView) {
        dailyTasksRecyclerView = dailyView.findViewById(R.id.dailyTasksRecyclerView);
        dailyTaskAdapter = new DailyTaskAdapter(dailyTaskList, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        dailyTasksRecyclerView.setLayoutManager(layoutManager);
        dailyTasksRecyclerView.setAdapter(dailyTaskAdapter);
    }

    private void initTodoRecyclerView(View todoView) {
        todoTasksRecyclerView = todoView.findViewById(R.id.todoTasksRecyclerView);
        todoAdapter = new TodoAdapter(todoTaskList, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        todoTasksRecyclerView.setLayoutManager(layoutManager);
        todoTasksRecyclerView.setAdapter(todoAdapter);
    }

    private void updateTabStates(Button activeTab) {
        Button[] tabs = {btnSchedule, btnTodo, btnDaily};
        for (Button tab : tabs) {
            tab.setSelected(tab == activeTab);
        }
    }

    // DailyTaskAdapter接口实现
    @Override
    public void onAddTaskClick() {
        showAddTaskDialog();
    }

    @Override
    public void onTaskCompleteClick(DailyTask task, boolean completed) {
        try {
            dailyTaskManager.markTaskCompleted(task, completed);
        } catch (Exception e) {
            return; 
        }

        try {
            dailyTaskAdapter.notifyDataSetChanged();
        } catch (Exception ignored) {

        }
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

        builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String taskContent = input.getText().toString().trim();
                if (!taskContent.isEmpty()) {
                    DailyTask newTask = new DailyTask(-1, taskContent);
                    dailyTaskManager.addTask(newTask);
                    dailyTaskAdapter.notifyItemInserted(0);
                }
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showDeleteConfirmationDialog(final DailyTask task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认删除");
        builder.setMessage("确定要删除这个任务吗？");

        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int position = dailyTaskList.indexOf(task);
                if (position != -1) {
                    dailyTaskManager.deleteTask(task);
                    dailyTaskAdapter.notifyItemRemoved(position);
                }
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    // TodoAdapter接口实现
    @Override
    public void Todo_onAddTaskClick() {
        showAddTodoDialog();
    }

    @Override
    public void Todo_onMoveUpClick(TodoTask task) {
        todoManager.moveTaskUp(task);
        todoAdapter.notifyDataSetChanged();
    }

    @Override
    public void Todo_onDeleteClick(TodoTask task) {
        showDeleteTodoConfirmationDialog(task);
    }

    private void showAddTodoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加待办事项");

        final EditText input = new EditText(this);
        input.setHint("请输入待办内容");
        input.setMinLines(3);
        input.setGravity(View.TEXT_ALIGNMENT_TEXT_START);
        builder.setView(input);

        builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String taskContent = input.getText().toString().trim();
                if (!taskContent.isEmpty()) {
                    TodoTask newTask = new TodoTask(-1, taskContent);
                    todoManager.addTask(newTask);
                    todoAdapter.notifyDataSetChanged();
                }
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showDeleteTodoConfirmationDialog(final TodoTask task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认删除");
        builder.setMessage("确定要删除这个待办事项吗？");

        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int position = todoTaskList.indexOf(task);
                if (position != -1) {
                    todoManager.deleteTask(task);
                    todoAdapter.notifyItemRemoved(position);
                }
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