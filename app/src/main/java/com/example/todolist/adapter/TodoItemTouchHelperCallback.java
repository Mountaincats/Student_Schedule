package com.example.todolist.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class TodoItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final TodoAdapter adapter;
    private boolean isDragging = false;

    public TodoItemTouchHelperCallback(TodoAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // 只允许上下拖拽，不允许滑动删除
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();

        // 确保位置有效且不是添加按钮
        if (fromPosition < 0 || toPosition < 0 ||
                fromPosition >= adapter.getItemCount() - 1 ||
                toPosition >= adapter.getItemCount() - 1) {
            return false;
        }

        // 直接移动项目，而不是交换相邻项
        adapter.onItemMoveDirectly(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // 不支持滑动删除
    }

    @Override
    public boolean isLongPressDragEnabled() {
        // 启用长按拖拽
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        // 禁用滑动
        return false;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            isDragging = true;
            // 设置拖拽状态
            if (viewHolder != null) {
                viewHolder.itemView.setAlpha(0.7f);
            }
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            isDragging = false;
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        // 恢复透明度
        viewHolder.itemView.setAlpha(1.0f);

        // 拖拽结束时保存优先级
        if (isDragging) {
            adapter.onDragCompleted();
            isDragging = false;
        }
    }
}