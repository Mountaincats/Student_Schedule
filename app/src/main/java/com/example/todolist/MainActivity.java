package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    private ImageButton btnSettings;
    private TextView btnSchedule, btnTodo, btnDaily;
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

        // 加载课表布局
        View scheduleView = getLayoutInflater().inflate(R.layout.layout_schedule, contentFrame, false);
        contentFrame.addView(scheduleView);

        // 更新页签状态
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
    }

    private void updateTabStates(TextView activeTab) {
        // 重置所有页签状态
        TextView[] tabs = {btnSchedule, btnTodo, btnDaily};
        for (TextView tab : tabs) {
            tab.setSelected(tab == activeTab);
            // 根据选中状态调整文字颜色
            if (tab == activeTab) {
                tab.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                tab.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }
}