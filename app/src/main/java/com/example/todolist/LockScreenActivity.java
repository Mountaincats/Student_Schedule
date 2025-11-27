package com.example.todolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LockScreenActivity extends AppCompatActivity {
    private EditText passwordInput;
    private Button unlockButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        passwordInput = findViewById(R.id.passwordInput);
        unlockButton = findViewById(R.id.unlockButton);
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // 检查是否已设置密码
        String savedPassword = sharedPreferences.getString("app_password", null);
        if (savedPassword == null) {
            // 首次使用，直接进入主界面
            startMainActivity();
            return;
        }

        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputPassword = passwordInput.getText().toString();
                if (inputPassword.equals(savedPassword)) {
                    startMainActivity();
                } else {
                    Toast.makeText(LockScreenActivity.this, R.string.wrong_password, Toast.LENGTH_SHORT).show();
                    passwordInput.setText("");
                }
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(LockScreenActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}