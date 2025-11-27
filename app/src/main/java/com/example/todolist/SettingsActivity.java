package com.example.todolist;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private EditText newPasswordInput, confirmPasswordInput;
    private Button saveButton, clearButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePassword();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearPassword();
            }
        });
    }

    private void initViews() {
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        saveButton = findViewById(R.id.saveButton);
        clearButton = findViewById(R.id.clearButton);
    }

    private void savePassword() {
        String newPassword = newPasswordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (newPassword.isEmpty()) {
            Toast.makeText(this, R.string.password_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, R.string.passwords_not_match, Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("app_password", newPassword);
        editor.apply();

        Toast.makeText(this, R.string.password_set_success, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void clearPassword() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("app_password");
        editor.apply();

        Toast.makeText(this, R.string.password_cleared, Toast.LENGTH_SHORT).show();
        finish();
    }
}