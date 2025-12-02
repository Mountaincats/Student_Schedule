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
    }

    // 批量更新任务优先级
    public void updateTasksPriorities(List<TodoTask> tasks) {
        todoDao.updateTasksInTransaction(tasks);
        // 重新加载数据
        loadData();
    }

    public void deleteTask(TodoTask task) {
        todoDao.deleteTask(task);
        // 从内存列表中移除
        todoTaskList.remove(task);
    }

    public int getNextTaskId() {
        return -1; // 数据库使用自增ID
    }
}