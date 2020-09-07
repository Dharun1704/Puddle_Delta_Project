package com.example.puddle;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout email;
    private TextInputLayout password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);

    }

    private boolean validateEmail() {
        String inputEmail = email.getEditText().getText().toString().trim();
        if (inputEmail.isEmpty()) {
            email.setError("Field can't be empty");
            return false;
        } else {
            email.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String inputPassword = password.getEditText().getText().toString().trim();
        if (inputPassword.isEmpty()) {
            password.setError("Field can't be empty");
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

    public void confirmInputs(View v) {
        if (!validateEmail() | !validatePassword()) {
            return;
        }

    }
}
