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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todolist.adapter.DailyTaskAdapter;
import com.example.todolist.adapter.TodoAdapter;
import com.example.todolist.adapter.TodoItemTouchHelperCallback;
import com.example.todolist.data.DailyTaskManager;
import com.example.todolist.data.Data;
import com.example.todolist.data.TodoManager;
import com.example.todolist.model.DailyTask;
import com.example.todolist.model.TodoTask;
import com.example.todolist.ui.ScheduleFragment;
import com.example.todolist.ui.dialog.ConfirmDeleteDialog;
import com.example.todolist.ui.dialog.DailyTaskDialog;
import com.example.todolist.ui.dialog.TodoTaskDialog;

import java.util.List;

public class MainActivity extends AppCompatActivity implements
        DailyTaskAdapter.OnTaskClickListener,
        TodoAdapter.OnTodoTaskClickListener,
        DailyTaskDialog.DailyTaskListener,
        TodoTaskDialog.TodoTaskListener,
        ConfirmDeleteDialog.ConfirmDeleteListener {
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
    private ItemTouchHelper itemTouchHelper;

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
                // 防止重复点击刷新
                if (!v.isSelected()) {
                    showScheduleView();
                }
            }
        });

        btnTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!v.isSelected()) {
                    showTodoView();
                }
            }
        });

        btnDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!v.isSelected()) {
                    showDailyView();
                }
            }
        });
    }

    // ================= 日程表逻辑 (Schedule) =================

    private void showScheduleView() {
        // 切换到 ScheduleFragment
        contentFrame.removeAllViews();
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentFrame, new ScheduleFragment())
                .commit();
                
        updateTabStates(btnSchedule);
    }

    // ================= 其他 Tab 逻辑 =================

    private void showTodoView() {
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

        // 设置ItemTouchHelper用于拖拽
        ItemTouchHelper.Callback callback = new TodoItemTouchHelperCallback(todoAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(todoTasksRecyclerView);

        // 将touchHelper传递给适配器
        todoAdapter.setTouchHelper(itemTouchHelper);
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
        showAddDailyTaskDialog();
    }

    @Override
    public void onEditTaskClick(DailyTask task) {
        showEditDailyTaskDialog(task);
    }

    @Override
    public void onTaskCompleteClick(DailyTask task, boolean completed) {
        try {
            dailyTaskManager.markTaskCompleted(task, completed);
        } catch (Exception e) {
            return; // 如果出错，提前返回
        }

        try {
            dailyTaskAdapter.notifyDataSetChanged();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onTaskDeleteClick(DailyTask task) {
        showDeleteDailyConfirmationDialog(task);
    }

    // TodoAdapter接口实现
    @Override
    public void Todo_onAddTaskClick() {
        showAddTodoDialog();
    }

    @Override
    public void Todo_onEditTaskClick(TodoTask task) {
        showEditTodoDialog(task);
    }

    @Override
    public void Todo_onDeleteClick(TodoTask task) {
        showDeleteTodoConfirmationDialog(task);
    }

    @Override
    public void Todo_onPriorityChange(List<TodoTask> tasks) {
        // 当优先级改变时，更新到数据库
        todoManager.updateTasksPriorities(tasks);
    }

    // DailyTaskDialog接口实现
    @Override
    public void onDailyTaskAdded(String content) {
        DailyTask newTask = new DailyTask(-1, content);
        dailyTaskManager.addTask(newTask);
        dailyTaskAdapter.notifyItemInserted(0);
    }

    @Override
    public void onDailyTaskEdited(DailyTask task, String newContent) {
        task.setContent(newContent);
        dailyTaskManager.updateTask(task);

        int position = dailyTaskList.indexOf(task);
        if (position != -1) {
            dailyTaskAdapter.notifyItemChanged(position);
        }
    }

    // TodoTaskDialog接口实现
    @Override
    public void onTodoTaskAdded(String content) {
        TodoTask newTask = new TodoTask(-1, content);
        todoManager.addTask(newTask);
        todoTaskList = todoManager.getTodoTasks();
        todoAdapter.updateData(todoTaskList);
    }

    @Override
    public void onTodoTaskEdited(TodoTask task, String newContent) {
        task.setContent(newContent);
        todoManager.updateTask(task);

        int position = todoTaskList.indexOf(task);
        if (position != -1) {
            todoAdapter.notifyItemChanged(position);
        }
    }

    // ConfirmDeleteDialog接口实现
    @Override
    public void onConfirmDelete() {
        // 这个实现需要根据具体删除的对象来处理
        // 我们会在showDeleteConfirmationDialog中设置具体的删除逻辑
    }

    // 使用新的对话框显示方法
    private void showAddDailyTaskDialog() {
        DailyTaskDialog dialog = DailyTaskDialog.newAddInstance();
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "add_daily_task");
    }

    private void showEditDailyTaskDialog(DailyTask task) {
        DailyTaskDialog dialog = DailyTaskDialog.newEditInstance(task);
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "edit_daily_task");
    }

    private void showDeleteDailyConfirmationDialog(final DailyTask task) {
        ConfirmDeleteDialog dialog = ConfirmDeleteDialog.newInstance(
                "确认删除",
                "确定要删除这个每日任务吗？"
        );
        dialog.setListener(new ConfirmDeleteDialog.ConfirmDeleteListener() {
            @Override
            public void onConfirmDelete() {
                int position = dailyTaskList.indexOf(task);
                if (position != -1) {
                    dailyTaskManager.deleteTask(task);
                    dailyTaskAdapter.notifyItemRemoved(position);
                }
            }
        });
        dialog.show(getSupportFragmentManager(), "delete_daily_task");
    }

    private void showAddTodoDialog() {
        TodoTaskDialog dialog = TodoTaskDialog.newAddInstance();
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "add_todo_task");
    }

    private void showEditTodoDialog(final TodoTask task) {
        TodoTaskDialog dialog = TodoTaskDialog.newEditInstance(task);
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "edit_todo_task");
    }

    private void showDeleteTodoConfirmationDialog(final TodoTask task) {
        ConfirmDeleteDialog dialog = ConfirmDeleteDialog.newInstance(
                "确认删除",
                "确定要删除这个待办事项吗？"
        );
        dialog.setListener(new ConfirmDeleteDialog.ConfirmDeleteListener() {
            @Override
            public void onConfirmDelete() {
                todoManager.deleteTask(task);
                todoTaskList = todoManager.getTodoTasks();
                todoAdapter.updateData(todoTaskList);
            }
        });
        dialog.show(getSupportFragmentManager(), "delete_todo_task");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dailyTaskManager != null) {
            dailyTaskManager.saveData();
        }
    }
}