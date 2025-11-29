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
            // 新任务添加到列表开头，然后重新排序
            todoTaskList.add(0, task);
            sortTasksByPriority();
        }
    }

    public void updateTask(TodoTask task) {
        todoDao.updateTask(task);
        sortTasksByPriority();
    }

    public void deleteTask(TodoTask task) {
        todoDao.deleteTask(task);
        todoTaskList.remove(task);
    }

    // 提升任务优先级（向上移动）
    public void moveTaskUp(TodoTask task) {
        int currentIndex = todoTaskList.indexOf(task);
        if (currentIndex > 0) { // 不是第一个才能上移
            TodoTask previousTask = todoTaskList.get(currentIndex - 1);

            // 交换优先级
            int tempPriority = task.getPriority();
            task.setPriority(previousTask.getPriority());
            previousTask.setPriority(tempPriority);

            // 更新数据库
            todoDao.updateTask(task);
            todoDao.updateTask(previousTask);

            // 重新排序列表
            sortTasksByPriority();
        }
    }

    // 按优先级排序
    private void sortTasksByPriority() {
        todoTaskList.sort((task1, task2) -> {
            int priorityCompare = Integer.compare(task1.getPriority(), task2.getPriority());
            if (priorityCompare == 0) {
                return Long.compare(task2.getCreatedTime(), task1.getCreatedTime());
            }
            return priorityCompare;
        });
    }

    public int getNextTaskId() {
        return -1; // 数据库使用自增ID
    }
}