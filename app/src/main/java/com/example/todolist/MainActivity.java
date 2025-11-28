package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    private ImageButton btnSettings;
    private Button btnSchedule, btnTodo, btnDaily;
    private FrameLayout contentFrame;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    private void showScheduleView() {
        // 清除之前的内容
        contentFrame.removeAllViews();

        // 加载课表布局（暂时显示简单文本）
        View scheduleView = getLayoutInflater().inflate(R.layout.layout_schedule, contentFrame, false);
        contentFrame.addView(scheduleView);

        // 更新按钮状态
        updateButtonStates(btnSchedule);
    }

    private void showTodoView() {
        contentFrame.removeAllViews();
        View todoView = getLayoutInflater().inflate(R.layout.layout_todo, contentFrame, false);
        contentFrame.addView(todoView);
        updateButtonStates(btnTodo);
    }

    private void showDailyView() {
        contentFrame.removeAllViews();
        View dailyView = getLayoutInflater().inflate(R.layout.layout_daily, contentFrame, false);
        contentFrame.addView(dailyView);
        updateButtonStates(btnDaily);
    }

    private void updateButtonStates(Button activeButton) {
        // 重置所有按钮状态
        Button[] buttons = {btnSchedule, btnTodo, btnDaily};
        for (Button btn : buttons) {
            btn.setSelected(btn == activeButton);
        }
    }
}