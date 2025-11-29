package com.example.todolist.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todolist.R;
import com.example.todolist.model.Schedule;
import java.util.List;
import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
    private List<Schedule> schedules;

    public ScheduleAdapter(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    public void updateData(List<Schedule> newSchedules) {
        this.schedules = newSchedules;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Schedule schedule = schedules.get(position);

        // 格式化时间
        holder.tvStartTime.setText(formatTime(schedule.getStartTime()));
        holder.tvEndTime.setText(formatTime(schedule.getEndTime()));

        holder.tvName.setText(schedule.getName());
        
        // 简单处理 Note 显示，避免空指针
        if (schedule.getNote() != null) {
            holder.tvRoom.setText(schedule.getNote().toString());
        } else {
            holder.tvRoom.setText("");
        }

        // 设置颜色条
        holder.colorBar.setBackgroundColor(schedule.getColorArgb());
    }

    @Override
    public int getItemCount() {
        return schedules == null ? 0 : schedules.size();
    }

    private String formatTime(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", h, m);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStartTime, tvEndTime, tvName, tvRoom;
        View colorBar;

        public ViewHolder(View itemView) {
            super(itemView);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvEndTime = itemView.findViewById(R.id.tvEndTime);
            tvName = itemView.findViewById(R.id.tvScheduleName);
            tvRoom = itemView.findViewById(R.id.tvScheduleRoom);
            colorBar = itemView.findViewById(R.id.colorBar);
        }
    }
}