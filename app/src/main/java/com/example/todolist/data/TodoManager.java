package com.example.todolist.data;

import android.content.Context;
import com.example.todolist.model.TodoTask;
import java.util.List;

public class TodoManager {
    private TodoDao todoDao;
    private List<TodoTask> todoTaskList;

    public TodoManager(Context context) {
        todoDao = new TodoDao(context);
        loadData();
    }

    // 从数据库加载数据
    private void loadData() {
        todoTaskList = todoDao.getAllTasks();
    }

    // 公共方法
    public List<TodoTask> getTodoTasks() {
        return todoTaskList;
    }

    public void addTask(TodoTask task) {
        long newId = todoDao.insertTask(task);
        if (newId != -1) {
            task.setId((int) newId);
            // 重新加载数据以确保顺序正确
            loadData();
        }
    }

    public void updateTask(TodoTask task) {
        todoDao.updateTask(task);
        // 重新加载数据以确保顺序正确
        loadData();
    }

    public void deleteTask(TodoTask task) {
        todoDao.deleteTask(task);
        // 从内存列表中移除
        todoTaskList.remove(task);
    }

    // 提升任务优先级（向上移动）
    public void moveTaskUp(TodoTask task) {
        int currentIndex = findTaskIndexById(task.getId());
        if (currentIndex > 0) { // 不是第一个才能上移
            TodoTask previousTask = todoTaskList.get(currentIndex - 1);

            // 交换优先级
            int tempPriority = task.getPriority();
            task.setPriority(previousTask.getPriority());
            previousTask.setPriority(tempPriority);

            // 更新数据库
            todoDao.updateTask(task);
            todoDao.updateTask(previousTask);

            // 重新加载数据以确保顺序正确
            loadData();
        }
    }

    // 根据ID查找任务在列表中的索引
    private int findTaskIndexById(int taskId) {
        for (int i = 0; i < todoTaskList.size(); i++) {
            if (todoTaskList.get(i).getId() == taskId) {
                return i;
            }
        }
        return -1;
    }

    public int getNextTaskId() {
        return -1; // 数据库使用自增ID
    }
}