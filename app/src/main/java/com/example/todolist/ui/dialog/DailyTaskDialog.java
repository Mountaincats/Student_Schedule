package com.example.todolist.ui.dialog;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.todolist.R;
import com.example.todolist.model.DailyTask;

public class DailyTaskDialog extends BaseTaskDialog {

    private static final String ARG_MODE = "mode";
    private static final String ARG_TASK = "task";

    public static final int MODE_ADD = 0;
    public static final int MODE_EDIT = 1;

    private int mode = MODE_ADD;
    private DailyTask task;
    private DailyTaskListener listener;

    public interface DailyTaskListener {
        void onDailyTaskAdded(String content);
        void onDailyTaskEdited(DailyTask task, String newContent);
    }

    public static DailyTaskDialog newAddInstance() {
        DailyTaskDialog dialog = new DailyTaskDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_MODE, MODE_ADD);
        dialog.setArguments(args);
        return dialog;
    }

    public static DailyTaskDialog newEditInstance(DailyTask task) {
        DailyTaskDialog dialog = new DailyTaskDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_MODE, MODE_EDIT);
        args.putSerializable(ARG_TASK, task);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mode = getArguments().getInt(ARG_MODE, MODE_ADD);
            if (mode == MODE_EDIT) {
                task = (DailyTask) getArguments().getSerializable(ARG_TASK);
            }
        }

        // 设置对话框样式
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomDialog);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_task_editor;
    }

    @Override
    protected String getTitle() {
        return mode == MODE_ADD ? "添加每日任务" : "编辑每日任务";
    }

    @Override
    protected String getHint() {
        return "请输入任务内容";
    }

    @Override
    protected String getPositiveButtonText() {
        return mode == MODE_ADD ? "添加" : "保存";
    }

    @Override
    protected String getNegativeButtonText() {
        return "取消";
    }

    @Override
    protected String getInitialContent() {
        return mode == MODE_EDIT && task != null ? task.getContent() : "";
    }

    @Override
    protected void onPositiveButtonClick(String content) {
        if (listener != null) {
            if (mode == MODE_ADD) {
                listener.onDailyTaskAdded(content);
            } else if (mode == MODE_EDIT) {
                listener.onDailyTaskEdited(task, content);
            }
        }
    }

    public void setListener(DailyTaskListener listener) {
        this.listener = listener;
    }
}