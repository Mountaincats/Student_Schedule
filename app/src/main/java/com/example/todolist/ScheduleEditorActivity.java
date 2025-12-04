package com.example.todolist;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todolist.data.Data;
import com.example.todolist.model.Day;
import com.example.todolist.model.RepeatRule;
import com.example.todolist.model.Schedule;
import com.example.todolist.model.Week;
import com.example.todolist.ui.dialog.ColorPickerDialogFragment;
import com.example.todolist.ui.dialog.NoteEditorDialogFragment;
import java.time.LocalDate;
import java.util.Locale;

public class ScheduleEditorActivity extends AppCompatActivity 
        implements NoteEditorDialogFragment.NoteListener, 
                   ColorPickerDialogFragment.OnColorSelectedListener {

    private static final String TAG = "ScheduleEditorActivity";

    // UI 控件
    private TextView tvEditorTitle;
    private EditText etScheduleName;
    private NumberPicker npStartHour, npStartMinute;
    private NumberPicker npEndHour, npEndMinute;
    private Button btnEditNote;
    private Button btnEditColor;
    private TextView tvNotePreview;
    private View viewColorPreview;
    private Button btnSave;
    private Button btnCancel;
    
    // 临时存储的数据
    private String tempNoteName = "备注";
    private String tempNoteContent = "";
    private int tempColor = 0xFF2196F3; // 默认蓝色
    
    private boolean isEditMode = false;
    private String targetDateStr;
    private String oldName;
    private int oldStartTime, oldEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_editor);

        try {
            initViews();
            setupNumberPickers();
            handleIntent();
            setupListeners();
            updateNotePreview();
            updateColorPreview();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        tvEditorTitle = findViewById(R.id.tvEditorTitle);
        etScheduleName = findViewById(R.id.etScheduleName);
        npStartHour = findViewById(R.id.npStartHour);
        npStartMinute = findViewById(R.id.npStartMinute);
        npEndHour = findViewById(R.id.npEndHour);
        npEndMinute = findViewById(R.id.npEndMinute);
        
        btnEditNote = findViewById(R.id.btnEditNote);
        btnEditColor = findViewById(R.id.btnEditColor);
        tvNotePreview = findViewById(R.id.tvNotePreview);
        viewColorPreview = findViewById(R.id.viewColorPreview);
        
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }
    
    private void setupNumberPickers() {
        if (npStartHour == null) return; 
        
        npStartHour.setMinValue(0); npStartHour.setMaxValue(23);
        npStartMinute.setMinValue(0); npStartMinute.setMaxValue(59);
        npEndHour.setMinValue(0); npEndHour.setMaxValue(23);
        npEndMinute.setMinValue(0); npEndMinute.setMaxValue(59);
        
        NumberPicker.Formatter formatter = i -> String.format(Locale.getDefault(), "%02d", i);
        npStartHour.setFormatter(formatter);
        npStartMinute.setFormatter(formatter);
        npEndHour.setFormatter(formatter);
        npEndMinute.setFormatter(formatter);
        
        npStartHour.setValue(8);
        npStartMinute.setValue(0);
        npEndHour.setValue(10);
        npEndMinute.setValue(0);
    }
    
    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("isEditMode", false)) {
            isEditMode = true;
            if (tvEditorTitle != null) tvEditorTitle.setText("编辑行程");
            
            targetDateStr = intent.getStringExtra("targetDate");
            oldName = intent.getStringExtra("name");
            if (etScheduleName != null) etScheduleName.setText(oldName);
            
            oldStartTime = intent.getIntExtra("start", 480);
            oldEndTime = intent.getIntExtra("end", 600);
            
            if (npStartHour != null) {
                npStartHour.setValue(oldStartTime / 60);
                npStartMinute.setValue(oldStartTime % 60);
                npEndHour.setValue(oldEndTime / 60);
                npEndMinute.setValue(oldEndTime % 60);
            }
            
            tempColor = intent.getIntExtra("color", 0xFF2196F3);
            if (intent.hasExtra("noteName")) {
                tempNoteName = intent.getStringExtra("noteName");
                tempNoteContent = intent.getStringExtra("noteContent");
            }
        } else {
            // 新建模式
            if (tvEditorTitle != null) tvEditorTitle.setText("创建行程");
            if (intent != null) {
                targetDateStr = intent.getStringExtra("targetDate");
            }
        }
    }

    private void setupListeners() {
        if (btnEditNote != null) {
            btnEditNote.setOnClickListener(v -> {
                NoteEditorDialogFragment dialog = NoteEditorDialogFragment.newInstance(tempNoteName, tempNoteContent);
                dialog.show(getSupportFragmentManager(), "NoteEditor");
            });
        }
        if (btnEditColor != null) {
            btnEditColor.setOnClickListener(v -> {
                ColorPickerDialogFragment dialog = ColorPickerDialogFragment.newInstance(tempColor);
                dialog.show(getSupportFragmentManager(), "ColorPicker");
            });
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveSchedule());
        }
    }
    
    @Override
    public void onNoteSaved(String title, String content) {
        this.tempNoteName = title;
        this.tempNoteContent = content;
        updateNotePreview();
    }

    @Override
    public void onColorSelected(int color) {
        this.tempColor = color;
        updateColorPreview();
    }
    
    private void saveSchedule() {
        String name = (etScheduleName != null && etScheduleName.getText() != null) ? etScheduleName.getText().toString().trim() : "";
        if (name.isEmpty()) name = "未命名行程";
        
        int startHour = npStartHour != null ? npStartHour.getValue() : 8;
        int startMinute = npStartMinute != null ? npStartMinute.getValue() : 0;
        int endHour = npEndHour != null ? npEndHour.getValue() : 10;
        int endMinute = npEndMinute != null ? npEndMinute.getValue() : 0;
        
        int startTime = startHour * 60 + startMinute;
        int endTime = endHour * 60 + endMinute;
        
        if (startTime >= endTime) endTime = startTime + 60;
        
        Schedule schedule = new Schedule(startTime, endTime, name);
        schedule.setColorArgb(tempColor);
        schedule.setNote(new Schedule.Note(tempNoteName, tempNoteContent));
        
        LocalDate date = (targetDateStr != null) ? LocalDate.parse(targetDateStr) : LocalDate.now();
        
        // 为了避免 Data.loadAllDataToWeek(week) 在 load 时就直接操作 week 导致逻辑复杂，
        // 这里我们简单地创建一个临时的 Week 对象来加载数据，然后找到对应的 Day
        Week tempWeek = new Week(date);
        Data.getInstance().loadAllDataToWeek(tempWeek);
        Day day = tempWeek.getDayForDate(date);
        
        if (day == null) {
            // 如果这天没数据，新建一个
            RepeatRule rule = new RepeatRule(RepeatRule.Mode.EVERY_N_WEEKS, 1, 0, date);
            day = new Day(date, false, rule);
        }
        
        if (isEditMode) {
            // 根据旧数据找到并删除旧行程
            // 注意：Schedule 需要正确实现 equals() 方法，或者我们需要更可靠的查找方式
            // 目前 Schedule.equals 比较 startTime, endTime, name。如果用户只改了颜色，可以匹配到。
            // 但如果用户改了时间或名称，oldName/oldStartTime 就是用来匹配的钥匙。
            Schedule oldSchedule = new Schedule(oldStartTime, oldEndTime, oldName);
            // 这里的 removeSchedule 依赖于 Schedule.equals
            // 如果 Schedule 类没有重写 equals，这里会失效。
            // 让我们确认 Schedule.java 是否重写了 equals。
            day.removeSchedule(oldSchedule); 
        }
        
        day.addSchedule(schedule);
        Data.getInstance().saveDay(day);
        setResult(RESULT_OK);
        finish();
    }

    private void updateNotePreview() {
        if (tvNotePreview != null) {
            if (tempNoteContent.isEmpty() && (tempNoteName.equals("备注") || tempNoteName.isEmpty())) {
                tvNotePreview.setText("无备注");
            } else {
                String preview = tempNoteName;
                if (!tempNoteContent.isEmpty()) {
                    preview += " - " + tempNoteContent;
                }
                tvNotePreview.setText(preview);
            }
        }
    }
    
    private void updateColorPreview() {
        if (viewColorPreview != null) {
            try {
                GradientDrawable drawable = (GradientDrawable) getDrawable(R.drawable.bg_circle_color);
                if (drawable != null) {
                    GradientDrawable newDrawable = (GradientDrawable) drawable.getConstantState().newDrawable().mutate();
                    newDrawable.setColor(tempColor);
                    viewColorPreview.setBackground(newDrawable);
                } else {
                    viewColorPreview.setBackgroundColor(tempColor);
                }
            } catch (Exception e) {
                viewColorPreview.setBackgroundColor(tempColor);
            }
        }
    }
}