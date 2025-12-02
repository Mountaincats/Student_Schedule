package com.example.todolist;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
public class SettingMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_main);  // 关联布局文件

        // 找到“设置密码”按钮
        Button btnGoSetPwd = findViewById(R.id.btn_go_set_password);

        // 按钮点击跳转逻辑
        btnGoSetPwd.setOnClickListener(v -> {
            Intent intent = new Intent(SettingMainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // 返回主页按钮逻辑
        Button btnBackMain = findViewById(R.id.btn_back_main);
        btnBackMain.setOnClickListener(v -> {
            // 跳回主页MainActivity
            Intent intent = new Intent(SettingMainActivity.this, MainActivity.class);
            startActivity(intent);
            // 关闭当前中间页面，避免返回栈堆积
            finish();
        });
    }
}
