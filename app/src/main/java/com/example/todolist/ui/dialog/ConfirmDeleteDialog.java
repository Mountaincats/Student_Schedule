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
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.todolist.R;

public class ConfirmDeleteDialog extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";

    private String title;
    private String message;
    private ConfirmDeleteListener listener;

    public interface ConfirmDeleteListener {
        void onConfirmDelete();
    }

    public static ConfirmDeleteDialog newInstance(String title, String message) {
        ConfirmDeleteDialog dialog = new ConfirmDeleteDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE, "确认删除");
            message = getArguments().getString(ARG_MESSAGE, "确定要删除吗？");
        }
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomDialog);
    }

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
        View view = inflater.inflate(R.layout.dialog_confirm_delete, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView messageView = view.findViewById(R.id.dialogMessage);
        Button positiveButton = view.findViewById(R.id.btnPositive);
        Button negativeButton = view.findViewById(R.id.btnNegative);

        if (titleView != null) {
            titleView.setText(title);
        }

        if (messageView != null) {
            messageView.setText(message);
        }

        if (positiveButton != null) {
            positiveButton.setText("删除");
            positiveButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConfirmDelete();
                }
                dismiss();
            });
        }

        if (negativeButton != null) {
            negativeButton.setText("取消");
            negativeButton.setOnClickListener(v -> dismiss());
        }
    }

    public void setListener(ConfirmDeleteListener listener) {
        this.listener = listener;
    }
}