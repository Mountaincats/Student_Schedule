package com.example.todolist.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todolist.R;
import com.example.todolist.model.TodoTask;
import java.util.Collections;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ADD_BUTTON = 0;
    private static final int TYPE_TASK_ITEM = 1;

    private List<TodoTask> taskList;
    private OnTodoTaskClickListener listener;
    private ItemTouchHelper touchHelper;

    public TodoAdapter(List<TodoTask> taskList, OnTodoTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.touchHelper = touchHelper;
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

    // 更新数据并刷新
    public void updateData(List<TodoTask> newTaskList) {
        this.taskList = newTaskList;
        notifyDataSetChanged();
    }

    // 直接移动项目到指定位置
    public void onItemMoveDirectly(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(taskList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(taskList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    // 拖拽完成后的回调
    public void onDragCompleted() {
        // 更新优先级
        updatePriorities();
    }

    // 更新所有任务的优先级
    private void updatePriorities() {
        for (int i = 0; i < taskList.size(); i++) {
            taskList.get(i).setPriority(i);
        }

        // 通知优先级改变
        if (listener != null) {
            listener.Todo_onPriorityChange(taskList);
        }
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
        ImageView dragHandle;
        TextView taskNumber;
        TextView taskContent;
        Button deleteButton;

        TaskViewHolder(View itemView) {
            super(itemView);
            dragHandle = itemView.findViewById(R.id.dragHandle);
            taskNumber = itemView.findViewById(R.id.todoTaskNumber);
            taskContent = itemView.findViewById(R.id.todoTaskContent);
            deleteButton = itemView.findViewById(R.id.todoDeleteButton);

            // 设置拖拽手柄的长按监听
            dragHandle.setOnLongClickListener(v -> {
                if (touchHelper != null) {
                    touchHelper.startDrag(this);
                }
                return true;
            });

            // 整个项目也可以长按拖拽
            itemView.setOnLongClickListener(v -> {
                if (touchHelper != null) {
                    touchHelper.startDrag(this);
                }
                return true;
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
        void Todo_onDeleteClick(TodoTask task);
        void Todo_onPriorityChange(List<TodoTask> tasks);
    }
}