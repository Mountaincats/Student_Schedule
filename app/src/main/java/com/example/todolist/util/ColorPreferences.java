package com.example.todolist.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ColorPreferences {
    private static final String PREF_NAME = "color_prefs";
    private static final String KEY_SAVED_COLORS = "saved_colors";

    public static void saveColors(Context context, List<Integer> colors) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // 将 List<Integer> 转换为逗号分隔的字符串
        String colorString = colors.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        prefs.edit().putString(KEY_SAVED_COLORS, colorString).apply();
    }

    public static List<Integer> getSavedColors(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String colorString = prefs.getString(KEY_SAVED_COLORS, "");
        
        List<Integer> colors = new ArrayList<>();
        
        // 如果没有保存过，返回默认颜色列表
        if (TextUtils.isEmpty(colorString)) {
            colors.add(0xFFF44336); // Red
            colors.add(0xFFE91E63); // Pink
            colors.add(0xFF9C27B0); // Purple
            colors.add(0xFF673AB7); // Deep Purple
            colors.add(0xFF3F51B5); // Indigo
            colors.add(0xFF2196F3); // Blue
            colors.add(0xFF03A9F4); // Light Blue
            colors.add(0xFF00BCD4); // Cyan
            colors.add(0xFF009688); // Teal
            colors.add(0xFF4CAF50); // Green
            return colors;
        }

        try {
            return Arrays.stream(colorString.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}