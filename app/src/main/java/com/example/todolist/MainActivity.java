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
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todolist.adapter.DailyTaskAdapter;
import com.example.todolist.adapter.TodoAdapter;
import com.example.todolist.data.DailyTaskManager;
import com.example.todolist.data.Data;
import com.example.todolist.data.TodoManager;
import com.example.todolist.model.DailyTask;
import com.example.todolist.model.TodoTask;
import com.example.todolist.ui.ScheduleFragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 初始化数据库
        Data.init(getApplicationContext());

        // 初始化每日任务数据管理器
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
        // 切换到 ScheduleFragment
        // 注意：如果 contentFrame 里之前是 View，removeAllViews 会清除它们
        // 如果之前是 Fragment，replace 会替换它
        contentFrame.removeAllViews();
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentFrame, new ScheduleFragment())
                .commit();
                
        updateTabStates(btnSchedule);
    }

    // ================= 其他 Tab 逻辑 =================

    private void showTodoView() {
        // 移除 Fragment (如果有的话)，因为我采取了解耦的思路，所以ai自动添加了两段移除Fragment的代码，只有改变了两个函数，并且确定不影响函数原本功能的实现
        // 简单的做法是先 removeAllViews，虽然这不会 destroy Fragment，但会移除 Fragment 的 View
        // 更好的做法是先从 FragmentManager 中移除 Fragment，或者隐藏它
        // 这里为了保持与旧代码兼容，我们直接 removeAllViews，因为旧代码是直接 addView 的
        
        // 清理 Fragment 容器
        androidx.fragment.app.Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (currentFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(currentFragment).commitNow();
        }
        contentFrame.removeAllViews();
        
        View todoView = getLayoutInflater().inflate(R.layout.layout_todo, contentFrame, false);
        contentFrame.addView(todoView);
        updateTabStates(btnTodo);

        // 初始化待办事项RecyclerView
        initTodoRecyclerView(todoView);
    }

    private void showDailyView() {
        // 清理 Fragment
        androidx.fragment.app.Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (currentFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(currentFragment).commitNow();
        }
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
        // 重新获取任务列表并更新适配器
        todoTaskList = todoManager.getTodoTasks();
        todoAdapter.updateData(todoTaskList);
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
                    // 重新获取任务列表并更新适配器
                    todoTaskList = todoManager.getTodoTasks();
                    todoAdapter.updateData(todoTaskList);
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
                todoManager.deleteTask(task);
                // 重新获取任务列表并更新适配器
                todoTaskList = todoManager.getTodoTasks();
                todoAdapter.updateData(todoTaskList);
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