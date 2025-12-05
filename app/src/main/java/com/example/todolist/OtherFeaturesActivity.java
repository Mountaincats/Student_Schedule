package com.example.todolist;

import android.os.Bundle;
import android.os.Handler;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import java.util.Random;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class OtherFeaturesActivity extends AppCompatActivity {

    private final String[] foods = {"牛堡", "寿司", "麻辣烫", "披萨", "粉面", "粥点",
            "盒饭", "KFC", "麦", "烤盘饭", "西餐", "手抓饼", "螺蛳粉", "黄焖鸡米饭", "烤肉饭", "饺子",
            "馄饨", "米线", "盖浇饭", "炸鸡架", "手抓饼", "凉皮", "肉夹馍", "花甲粉", "冒菜", "烤鱼",
            "火锅", "串串香", "拌面", "蛋炒饭", "煲仔饭", "卤肉饭", "意面"};
    private AlertDialog foodDialog;
    private TextView tvFoodResult;
    private final Handler handler = new Handler();
    private Runnable randomRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_other_features);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 找到“今天吃什么”按钮，添加点击事件
        Button btnWhatToEat = findViewById(R.id.btnWhatToEat);
        btnWhatToEat.setOnClickListener(v -> showRandomFoodDialog());
    }

    // 显示随机食物弹窗
    private void showRandomFoodDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_random_food, null);
        tvFoodResult = dialogView.findViewById(R.id.tvFoodResult);

        foodDialog = new AlertDialog.Builder(this)
                .setTitle("随机选餐")
                .setView(dialogView)
                .setPositiveButton("好的", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("换一个",null)
                .create();

        foodDialog.setOnShowListener(dialog -> {
            Button btnChange = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
            btnChange.setOnClickListener(v -> {
                // 只执行重新随机，不关闭弹窗
                startRandomFood();
            });
        });

        startRandomFood();
        foodDialog.show();
    }

    // 随机食物闪动逻辑
    private void startRandomFood() {
        if (randomRunnable != null) {
            handler.removeCallbacks(randomRunnable);
        }

        tvFoodResult.setText("");

        randomRunnable = new Runnable() {
            int count = 0;
            @Override
            public void run() {
                if (count < 20) {
                    int randomIndex = new Random().nextInt(foods.length);
                    tvFoodResult.setText(foods[randomIndex]);
                    handler.postDelayed(this, 100);
                    count++;
                } else {
                    tvFoodResult.setText(tvFoodResult.getText() + "\n今天就吃这个吧");
                }
            }
        };
        handler.post(randomRunnable);
    }

    // Activity销毁时释放资源
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && randomRunnable != null) {
            handler.removeCallbacks(randomRunnable);
        }
    }
}