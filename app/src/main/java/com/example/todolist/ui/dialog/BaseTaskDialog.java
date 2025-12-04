package com.example.todolist.ui.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.todolist.R;

public abstract class BaseTaskDialog extends DialogFragment {

    protected EditText input;
    protected Button positiveButton;
    protected Button negativeButton;

    protected abstract int getLayoutResId();
    protected abstract String getTitle();
    protected abstract String getHint();
    protected abstract String getPositiveButtonText();
    protected abstract String getNegativeButtonText();
    protected abstract String getInitialContent();
    protected abstract void onPositiveButtonClick(String content);

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResId(), container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        // 设置标题
        TextView titleView = view.findViewById(R.id.dialogTitle);
        if (titleView != null) {
            titleView.setText(getTitle());
        }

        // 初始化输入框
        input = view.findViewById(R.id.dialogInput);
        if (input != null) {
            input.setHint(getHint());
            input.setText(getInitialContent());
            input.setSelection(input.getText().length()); // 光标移到末尾
        }

        // 初始化按钮
        positiveButton = view.findViewById(R.id.btnPositive);
        negativeButton = view.findViewById(R.id.btnNegative);

        if (positiveButton != null) {
            positiveButton.setText(getPositiveButtonText());
            positiveButton.setOnClickListener(v -> {
                String content = input.getText().toString().trim();
                if (!content.isEmpty()) {
                    onPositiveButtonClick(content);
                    dismiss();
                } else {
                    // 输入为空时的处理，可以添加提示
                    input.setError("请输入内容");
                }
            });
        }

        if (negativeButton != null) {
            negativeButton.setText(getNegativeButtonText());
            negativeButton.setOnClickListener(v -> dismiss());
        }
    }
}