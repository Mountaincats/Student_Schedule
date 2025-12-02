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
    private boolean isDragging = false;

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

    // 方案1：使用Collections.swap实现相邻交换（支持实时刷新）
    public void onItemSwap(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(taskList, i, i + 1);
                // 更新优先级
                taskList.get(i).setPriority(i);
                taskList.get(i + 1).setPriority(i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(taskList, i, i - 1);
                // 更新优先级
                taskList.get(i).setPriority(i);
                taskList.get(i - 1).setPriority(i - 1);
            }
        }

        // 通知移动和刷新序号
        notifyItemMoved(fromPosition, toPosition);

        // 刷新移动范围内的所有项目序号
        int start = Math.min(fromPosition, toPosition);
        int end = Math.max(fromPosition, toPosition);
        notifyItemRangeChanged(start, end - start + 1);

        // 立即更新优先级
        if (listener != null) {
            listener.Todo_onPriorityChange(taskList);
        }
    }

    // 方案2：直接移动项目到任意位置（支持任意位置移动）
    public void onItemMoveDirectly(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0 ||
                fromPosition >= taskList.size() || toPosition >= taskList.size()) {
            return;
        }

        // 保存要移动的项目
        TodoTask movedTask = taskList.get(fromPosition);

        // 从原位置移除
        taskList.remove(fromPosition);

        // 插入到新位置
        taskList.add(toPosition, movedTask);

        // 更新所有任务的优先级
        updateAllPriorities();

        // 通知项目移动
        notifyItemMoved(fromPosition, toPosition);

        // 刷新所有项目的序号（因为位置都变了）
        notifyItemRangeChanged(0, taskList.size());
    }

    // 更新所有任务的优先级
    private void updateAllPriorities() {
        for (int i = 0; i < taskList.size(); i++) {
            taskList.get(i).setPriority(i);
        }
    }

    // 拖拽开始
    public void onDragStart() {
        isDragging = true;
    }

    // 拖拽结束
    public void onDragEnd() {
        isDragging = false;
        // 拖拽结束时保存优先级
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
                    onDragStart();
                }
                return true;
            });

            // 整个项目也可以长按拖拽
            itemView.setOnLongClickListener(v -> {
                if (touchHelper != null) {
                    touchHelper.startDrag(this);
                    onDragStart();
                }
                return true;
            });

            // 设置任务内容的点击监听（用于编辑）
            taskContent.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && position < taskList.size()) {
                        listener.Todo_onEditTaskClick(taskList.get(position));
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

            // 设置拖拽状态
            if (isDragging) {
                itemView.setAlpha(0.7f);
            } else {
                itemView.setAlpha(1.0f);
            }
        }
    }

    public interface OnTodoTaskClickListener {
        void Todo_onAddTaskClick();
        void Todo_onEditTaskClick(TodoTask task); // 新增：编辑任务
        void Todo_onDeleteClick(TodoTask task);
        void Todo_onPriorityChange(List<TodoTask> tasks);
    }
}