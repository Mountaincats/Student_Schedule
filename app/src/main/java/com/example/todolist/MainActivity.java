package com.example.todolist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todolist.adapter.DailyTaskAdapter;
import com.example.todolist.model.DailyTask;

import java.util.ArrayList; // 确保这个导入存在
import java.util.Calendar;  // 添加这个导入
import java.util.List;

public class MainActivity extends AppCompatActivity implements DailyTaskAdapter.OnTaskClickListener {
    private ImageButton btnSettings;
    private Button btnSchedule, btnTodo, btnDaily;
    private FrameLayout contentFrame;
    private Toolbar toolbar;

    // 每日任务相关
    private RecyclerView dailyTasksRecyclerView;
    private DailyTaskAdapter dailyTaskAdapter;
    private List<DailyTask> dailyTaskList;
    private int taskIdCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
        initDailyTasks();

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

        // 初始化每日任务列表
        dailyTaskList = new ArrayList<>();
    }

    private void initDailyTasks() {
        // 这里可以添加一些示例任务用于测试
        // dailyTaskList.add(new DailyTask(taskIdCounter++, "示例任务"));
    }

    private void setupClickListeners() {
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置按钮点击事件
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

    private void showScheduleView() {
        contentFrame.removeAllViews();
        View scheduleView = getLayoutInflater().inflate(R.layout.layout_schedule, contentFrame, false);
        contentFrame.addView(scheduleView);
        updateTabStates(btnSchedule);
    }

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
        task.setCompletedToday(completed);

        // 如果是完成，记录到当前周
        if (completed) {
            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 周日=0, 周一=1, ...
            task.markCompleted(0, dayOfWeek); // 0表示当前周
        }

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

        builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String taskContent = input.getText().toString().trim();
                if (!taskContent.isEmpty()) {
                    DailyTask newTask = new DailyTask(taskIdCounter++, taskContent);
                    dailyTaskList.add(0, newTask); // 修复：在索引0处添加新任务
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
                    dailyTaskList.remove(position);
                    dailyTaskAdapter.notifyItemRemoved(position);
                }
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }
}