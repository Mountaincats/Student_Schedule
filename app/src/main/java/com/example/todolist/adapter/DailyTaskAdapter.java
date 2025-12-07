package com.example.todolist.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todolist.R;
import com.example.todolist.model.DailyTask;
import java.util.List;

public class DailyTaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ADD_BUTTON = 0;
    private static final int TYPE_TASK_ITEM = 1;

    private List<DailyTask> taskList;
    private OnTaskClickListener listener;

    public DailyTaskAdapter(List<DailyTask> taskList, OnTaskClickListener listener) {
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
            View view = inflater.inflate(R.layout.item_add_daily_task, parent, false);
            return new AddButtonViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_daily_task, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_ADD_BUTTON) {
            ((AddButtonViewHolder) holder).bind();
        } else {
            DailyTask task = taskList.get(position);
            ((TaskViewHolder) holder).bind(task);
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
            addButton = itemView.findViewById(R.id.btnAddTask);

            addButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddTaskClick();
                }
            });
        }

        void bind() {
            // 添加按钮不需要绑定数据
        }
    }

    // 任务项的ViewHolder
    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskContent;
        CheckBox completionCheckbox;
        Button deleteButton;
        LinearLayout weekIndicatorLayout;

        TaskViewHolder(View itemView) {
            super(itemView);
            taskContent = itemView.findViewById(R.id.taskContent);
            completionCheckbox = itemView.findViewById(R.id.completionCheckbox);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            weekIndicatorLayout = itemView.findViewById(R.id.weekIndicatorLayout);

            // 设置任务内容的点击监听（用于编辑）
            taskContent.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && position < taskList.size()) {
                        listener.onEditTaskClick(taskList.get(position));
                    }
                }
            });

            completionCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && position < taskList.size()) {
                        // 先取消监听，防止重复触发
                        completionCheckbox.setOnCheckedChangeListener(null);
                        // 设置新的状态
                        completionCheckbox.setChecked(isChecked);
                        // 重新设置监听
                        completionCheckbox.setOnCheckedChangeListener((buttonView2, isChecked2) -> {
                            if (listener != null) {
                                int pos = getAdapterPosition();
                                if (pos != RecyclerView.NO_POSITION && pos < taskList.size()) {
                                    listener.onTaskCompleteClick(taskList.get(pos), isChecked2);
                                }
                            }
                        });

                        // 通知监听器
                        listener.onTaskCompleteClick(taskList.get(position), isChecked);
                    }
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && position < taskList.size()) {
                        listener.onTaskDeleteClick(taskList.get(position));
                    }
                }
            });
        }

        void bind(DailyTask task) {
            taskContent.setText(task.getContent());
            // 先取消监听，防止触发
            completionCheckbox.setOnCheckedChangeListener(null);
            completionCheckbox.setChecked(task.isCompletedToday());
            // 重新设置监听
            completionCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && position < taskList.size()) {
                        listener.onTaskCompleteClick(taskList.get(position), isChecked);
                    }
                }
            });

            updateWeekIndicators(task);
        }

        private void updateWeekIndicators(DailyTask task) {
            weekIndicatorLayout.removeAllViews();

            for (int i = 0; i < 10; i++) {
                View weekView = new View(itemView.getContext());
                int totalCompletionCount = task.getWeekTotalCompletionCount(i);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0, // width will be set by weight
                        20, // height in dp
                        1.0f // weight
                );
                params.setMargins(2, 0, 2, 0);
                weekView.setLayoutParams(params);

                // 根据完成次数设置颜色
                int color = getColorForCompletion(totalCompletionCount);
                weekView.setBackgroundColor(color);

                weekIndicatorLayout.addView(weekView);
            }
        }

        /**
         * 根据完成次数返回不同的颜色
         * 0次: 灰色 (#E0E0E0)
         * 1-3次: 蓝色渐变，从浅蓝到深蓝
         * 4-6次: 绿色渐变，从浅绿到深绿
         * 7次及以上: 金色
         */
        private int getColorForCompletion(int totalCompletionCount) {
            if (totalCompletionCount <= 0) {
                return 0xFFE0E0E0; // 灰色
            } else if (totalCompletionCount <= 3) {
                // 蓝色渐变：1-3次
                return getBlueColorForCount(totalCompletionCount, 3);
            } else if (totalCompletionCount <= 6) {
                // 绿色渐变：4-6次
                return getGreenColorForCount(totalCompletionCount, 3, 6);
            } else {
                return 0xFFFFD700; // 金色
            }
        }

        /**
         * 获取蓝色渐变颜色
         * @param count 当前完成次数
         * @param maxCount 最大次数
         */
        private int getBlueColorForCount(int count, int maxCount) {
            // 蓝色基础颜色：从浅蓝(0xFF81D4FA)到深蓝(0xFF1976D2)
            float ratio = (float) count / maxCount;

            int startColor = 0xFF81D4FA; // 浅蓝色
            int endColor = 0xFF1976D2;   // 深蓝色

            int startRed = (startColor >> 16) & 0xFF;
            int startGreen = (startColor >> 8) & 0xFF;
            int startBlue = startColor & 0xFF;

            int endRed = (endColor >> 16) & 0xFF;
            int endGreen = (endColor >> 8) & 0xFF;
            int endBlue = endColor & 0xFF;

            int red = (int) (startRed + (endRed - startRed) * ratio);
            int green = (int) (startGreen + (endGreen - startGreen) * ratio);
            int blue = (int) (startBlue + (endBlue - startBlue) * ratio);

            return 0xFF000000 | (red << 16) | (green << 8) | blue;
        }

        /**
         * 获取绿色渐变颜色
         * @param count 当前完成次数
         * @param minCount 最小次数
         * @param maxCount 最大次数
         */
        private int getGreenColorForCount(int count, int minCount, int maxCount) {
            // 绿色基础颜色：从浅绿(0xFFA5D6A7)到深绿(0xFF2E7D32)
            float ratio = (float) (count - minCount) / (maxCount - minCount);

            int startColor = 0xFFA5D6A7; // 浅绿色
            int endColor = 0xFF2E7D32;   // 深绿色

            int startRed = (startColor >> 16) & 0xFF;
            int startGreen = (startColor >> 8) & 0xFF;
            int startBlue = startColor & 0xFF;

            int endRed = (endColor >> 16) & 0xFF;
            int endGreen = (endColor >> 8) & 0xFF;
            int endBlue = endColor & 0xFF;

            int red = (int) (startRed + (endRed - startRed) * ratio);
            int green = (int) (startGreen + (endGreen - startGreen) * ratio);
            int blue = (int) (startBlue + (endBlue - startBlue) * ratio);

            return 0xFF000000 | (red << 16) | (green << 8) | blue;
        }
    }

    public interface OnTaskClickListener {
        void onAddTaskClick();
        void onEditTaskClick(DailyTask task);
        void onTaskCompleteClick(DailyTask task, boolean completed);
        void onTaskDeleteClick(DailyTask task);
    }
}