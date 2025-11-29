package com.example.todolist.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todolist.R;
import com.example.todolist.model.Day;
import com.example.todolist.model.Schedule;
import com.example.todolist.model.Week;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class DayPagerAdapter extends RecyclerView.Adapter<DayPagerAdapter.DayViewHolder> {
    private Week week; // 数据源：规则引擎
    private LocalDate baseDate; // 基准日期 (通常是今天)
    
    // 设定一个巨大的中间值作为起始位置，实现双向无限滑动
    public static final int START_POSITION = Integer.MAX_VALUE / 2;

    // 高度比例：每一分钟对应多少 dp
    private static final float DP_PER_MINUTE = 1.5f;
    // 左侧时间轴宽度 dp
    private static final int TIMELINE_WIDTH_DP = 50;
    // 顶部留白高度
    private static final int TOP_SPACE_DP = 80;
    // 底部留白高度
    private static final int BOTTOM_SPACE_DP = 80;

    public DayPagerAdapter(Week week, LocalDate baseDate) {
        this.week = week;
        this.baseDate = baseDate;
    }
    
    /**
     * 根据 position 计算对应的日期
     */
    public LocalDate getDateAtPosition(int position) {
        return baseDate.plusDays(position - START_POSITION);
    }

    /**
     * 根据日期反推 position
     */
    public int getPositionForDate(LocalDate date) {
        return START_POSITION + (int) ChronoUnit.DAYS.between(baseDate, date);
    }

    public void updateData(Week week) {
        this.week = week;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        // 动态计算当前页面的日期
        LocalDate date = getDateAtPosition(position);
        
        // 向 Week 请求这一天的数据
        Day day = week.getDayForDate(date);
        
        // 如果这一天没有数据，创建一个临时的 Day 用于显示空时间轴
        if (day == null) {
            day = new Day(date);
            day.setActiveHours(8, 22);
        }
        
        holder.bind(day);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE; // 无限滑动
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout container;
        View timelineGuide;

        DayViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.dayScheduleContainer);
            timelineGuide = itemView.findViewById(R.id.timeline_guide);
        }

        void bind(Day day) {
            container.removeAllViews();

            addHeaderView();
            addTimelineGuide();

            if (day == null) return;

            int startHour = day.getActiveStartHour();
            int endHour = day.getActiveEndHour();

            for (int h = startHour; h <= endHour; h++) {
                addTimeLabel(h, startHour);
                addTimeLine(h, startHour);
            }

            int totalMinutes = (endHour - startHour + 1) * 60;
            int contentHeight = (int)(totalMinutes * DP_PER_MINUTE);

            for (Schedule schedule : day.getSchedules()) {
                addScheduleBlock(schedule, startHour);
            }

            addFooterView(TOP_SPACE_DP + contentHeight);

            container.setMinimumHeight(dp2px(TOP_SPACE_DP + contentHeight + BOTTOM_SPACE_DP));

            if (container.getParent() instanceof ScrollView) {
                ScrollView scrollView = (ScrollView) container.getParent();
                scrollView.post(() -> {
                    int targetScrollY = dp2px(TOP_SPACE_DP - 30);
                    scrollView.scrollTo(0, Math.max(0, targetScrollY));
                });
            }
        }

        private void addHeaderView() {
            TextView tv = new TextView(itemView.getContext());
            tv.setText("没有更多行程");
            tv.setTextSize(12);
            tv.setTextColor(0xFF9E9E9E);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.setMargins(0, dp2px(20), 0, 0);
            container.addView(tv, params);
        }

        private void addFooterView(int topMarginDp) {
            TextView tv = new TextView(itemView.getContext());
            tv.setText("没有更多行程");
            tv.setTextSize(12);
            tv.setTextColor(0xFF9E9E9E);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.setMargins(0, dp2px(topMarginDp + 20), 0, 0);
            container.addView(tv, params);
        }

        private void addTimelineGuide() {
            View guide = new View(itemView.getContext());
            guide.setBackgroundColor(0xFFE0E0E0);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dp2px(1), ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(dp2px(TIMELINE_WIDTH_DP), dp2px(TOP_SPACE_DP - 10), 0, dp2px(BOTTOM_SPACE_DP - 10));
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            container.addView(guide);
        }

        private void addTimeLabel(int hour, int baseStartHour) {
            TextView tv = new TextView(itemView.getContext());
            tv.setText(String.format(Locale.getDefault(), "%02d:00", hour));
            tv.setTextSize(12);
            tv.setTextColor(Color.GRAY);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int marginTop = TOP_SPACE_DP + (int)((hour - baseStartHour) * 60 * DP_PER_MINUTE);
            params.setMargins(dp2px(8), dp2px(marginTop), 0, 0);
            container.addView(tv, params);
        }

        private void addTimeLine(int hour, int baseStartHour) {
            View line = new View(itemView.getContext());
            line.setBackgroundColor(0xFFEEEEEE);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(1));
            int marginTop = TOP_SPACE_DP + (int)((hour - baseStartHour) * 60 * DP_PER_MINUTE);
            params.setMargins(dp2px(TIMELINE_WIDTH_DP), dp2px(marginTop), 0, 0);
            container.addView(line, params);
        }

        private void addScheduleBlock(Schedule schedule, int baseStartHour) {
            int startMinutesFromBase = schedule.getStartTime() - (baseStartHour * 60);
            int duration = schedule.getEndTime() - schedule.getStartTime();
            if (startMinutesFromBase < 0) return;

            int marginTop = TOP_SPACE_DP + (int)(startMinutesFromBase * DP_PER_MINUTE);
            int height = (int)(duration * DP_PER_MINUTE);

            CardView card = new CardView(itemView.getContext());
            card.setCardBackgroundColor(schedule.getColorArgb());
            card.setRadius(dp2px(4));
            card.setCardElevation(dp2px(2));

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            params.setMargins(dp2px(TIMELINE_WIDTH_DP + 4), dp2px(marginTop), dp2px(8), 0);

            TextView tv = new TextView(itemView.getContext());
            tv.setText(schedule.getName() + "\n" + schedule.getNote().getName());
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(12);
            tv.setPadding(dp2px(4), dp2px(4), dp2px(4), dp2px(4));

            card.addView(tv);
            container.addView(card, params);
        }

        private int dp2px(float dp) {
            float density = itemView.getContext().getResources().getDisplayMetrics().density;
            return (int) (dp * density + 0.5f);
        }
    }
}