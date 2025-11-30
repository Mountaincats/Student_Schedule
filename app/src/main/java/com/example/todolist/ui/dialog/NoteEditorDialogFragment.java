package com.example.todolist.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.todolist.R;

public class NoteEditorDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_CONTENT = "content";

    private NoteListener listener;

    public interface NoteListener {
        void onNoteSaved(String title, String content);
    }

    public static NoteEditorDialogFragment newInstance(String title, String content) {
        NoteEditorDialogFragment fragment = new NoteEditorDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NoteListener) {
            listener = (NoteListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement NoteListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_note_editor, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initViews(view, dialog);

        return dialog;
    }

    private void initViews(View view, AlertDialog dialog) {
        EditText etContent = view.findViewById(R.id.etNoteContent);
        Button btnSave = view.findViewById(R.id.btnSaveNote);
        Button btnCancel = view.findViewById(R.id.btnCancelNote);

        // 回显数据
        String title = getArguments().getString(ARG_TITLE, "");
        String content = getArguments().getString(ARG_CONTENT, "");
        StringBuilder sb = new StringBuilder();
        if (!"备注".equals(title) && !title.isEmpty()) {
            sb.append(title);
        }
        if (!content.isEmpty()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(content);
        }
        etContent.setText(sb.toString());
        etContent.setSelection(etContent.getText().length());

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String fullText = etContent.getText().toString().trim();
            String newTitle = "备注";
            String newContent = "";

            if (!fullText.isEmpty()) {
                String[] parts = fullText.split("\n", 2);
                newTitle = parts[0].trim();
                if (newTitle.isEmpty()) newTitle = "备注";
                
                if (parts.length > 1) {
                    newContent = parts[1].trim();
                }
            }

            if (listener != null) {
                listener.onNoteSaved(newTitle, newContent);
            }
            dismiss();
        });
    }
}