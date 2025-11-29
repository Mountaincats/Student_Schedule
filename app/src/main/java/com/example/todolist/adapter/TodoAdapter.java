package com.example.todolist.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todolist.R;
import com.example.todolist.model.TodoTask;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ADD_BUTTON = 0;
    private static final int TYPE_TASK_ITEM = 1;

    private List<TodoTask> taskList;
    private OnTodoTaskClickListener listener;

    public TodoAdapter(List<TodoTask> taskList, OnTodoTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return position == taskList.size() ? TYPE_ADD_BUTTON : TYPE_TASK_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_ADD_BUTTON) {
            View view = inflater.inflate(R.layout.item_add_todo_task, parent, false);
            return new AddButtonViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_todo_task, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_ADD_BUTTON) {
            ((AddButtonViewHolder) holder).bind();
        } else {
            TodoTask task = taskList.get(position);
            ((TaskViewHolder) holder).bind(task, position);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size() + 1; // +1 for add button
    }

    // 添加按钮的ViewHolder
    class AddButtonViewHolder extends RecyclerView.ViewHolder {
        Button addButton;

        AddButtonViewHolder(View itemView) {
            super(itemView);
            addButton = itemView.findViewById(R.id.btnAddTodoTask);

            addButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.Todo_onAddTaskClick();
                }
            });
        }

        void bind() {
            // 添加按钮不需要绑定数据
        }
    }

    // 任务项的ViewHolder
    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskNumber;
        TextView taskContent;
        Button upButton;
        Button deleteButton;

        TaskViewHolder(View itemView) {
            super(itemView);
            taskNumber = itemView.findViewById(R.id.todoTaskNumber);
            taskContent = itemView.findViewById(R.id.todoTaskContent);
            upButton = itemView.findViewById(R.id.todoUpButton);
            deleteButton = itemView.findViewById(R.id.todoDeleteButton);

            upButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && position < taskList.size()) {
                        listener.Todo_onMoveUpClick(taskList.get(position));
                    }
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && position < taskList.size()) {
                        listener.Todo_onDeleteClick(taskList.get(position));
                    }
                }
            });
        }

        void bind(TodoTask task, int position) {
            // 显示序号（位置+1）
            taskNumber.setText(String.valueOf(position + 1));
            taskContent.setText(task.getContent());
        }
    }

    public interface OnTodoTaskClickListener {
        void Todo_onAddTaskClick();
        void Todo_onMoveUpClick(TodoTask task);
        void Todo_onDeleteClick(TodoTask task);
    }
}