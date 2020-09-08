package com.example.puddle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {

    private DatabaseReference reference;

    private TextInputLayout username, password;
    private CardView loginCard, signUpCard;
    private TextInputLayout createEmail, createUsername, createPassword;
    private TextView signUpText;
    private Button logIn, signUp;

    private long userId = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        loginCard = findViewById(R.id.loginCard);
        signUpCard = findViewById(R.id.signUpCard);

        username = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        createEmail = findViewById(R.id.create_email);
        createUsername = findViewById(R.id.create_username);
        createPassword = findViewById(R.id.create_password);
        signUpText = findViewById(R.id.signUpText);
        logIn = findViewById(R.id.login_button);
        signUp = findViewById(R.id.signup_button);

        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userId = dataSnapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String encPwd = "";
                try {
                      encPwd = startEncryption(password.getEditText().getText().toString().trim());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                confirmInputs(username.getEditText().getText().toString().trim(),
                        encPwd);
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    confirmSignUp(createEmail.getEditText().getText().toString().trim(),
                            createUsername.getEditText().getText().toString().trim(),
                            createPassword.getEditText().getText().toString().trim());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Animation fadeIn = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade_in);
                final Animation fadeOut = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade_out);
                loginCard.setAnimation(fadeOut);
                loginCard.setVisibility(View.GONE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        signUpCard.setVisibility(View.VISIBLE);
                        signUpCard.setAnimation(fadeIn);
                    }
                }, 500);
            }
        });
    }

    private boolean validateUsername() {
        String inputEmail = username.getEditText().getText().toString().trim();
        if (inputEmail.isEmpty()) {
            username.setError("Field can't be empty");
            return false;
        } else {
            username.setError(null);
            return true;
        }
    }

    private boolean validateSignUpEmail() {
        String inputEmail = createEmail.getEditText().getText().toString().trim();
        if (inputEmail.isEmpty()) {
            createEmail.setError("Field can't be empty");
            return false;
        } else {
            createEmail.setError(null);
            return true;
        }
    }

    private boolean validateSignUpUsername() {
        String username = createUsername.getEditText().getText().toString().trim();
        if (username.isEmpty()) {
            createUsername.setError("Field can't be empty");
            return false;
        } else {
            createUsername.setError(null);
            return true;
        }
    }

    private boolean validateSignUpPassword() {
        String inputPassword = createPassword.getEditText().getText().toString().trim();
        if (inputPassword.isEmpty()) {
            createPassword.setError("Field can't be empty");
            return false;
        } else {
            createPassword.setError(null);
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

    public void confirmInputs(final String username, final String password) {
        if (!validateUsername() | !validatePassword()) {
            return;
        }

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(username).exists()) {
                    if (!username.isEmpty()) {
                        User user = dataSnapshot.child(username).getValue(User.class);
                        if (user.getPassword().equals(password)) {
                            Toast.makeText(LoginActivity.this, "LogIn successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, NewsActivity.class);
                            intent.putExtra("user", username);
                            startActivity(intent);
                        } else {
                            Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "User not found! Please create an account",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User not found! Please create an account",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void confirmSignUp(String email, String username, String password) throws Exception {
        if (!validateSignUpEmail() | !validateSignUpUsername() | !validateSignUpPassword()) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        String pwd = startEncryption(password);
        user.setPassword(pwd);
        reference.child(username).setValue(user);
        Toast.makeText(LoginActivity.this, "Account created successfully. Please logIn.",
                Toast.LENGTH_SHORT).show();

        final Animation fadeIn = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade_in);
        final Animation fadeOut = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade_out);
        signUpCard.setAnimation(fadeOut);
        signUpCard.setVisibility(View.GONE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loginCard.setVisibility(View.VISIBLE);
                loginCard.setAnimation(fadeIn);
            }
        }, 500);
    }

    private String startEncryption(String password) throws Exception {
        byte[] input = password.getBytes();
        byte[] output = new byte[0];

        output = encrypt(input, "SHA-1");
        BigInteger shaData = new BigInteger(1, output);
        return shaData.toString(16);
    }

    private byte[] encrypt(byte[] data, String string) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(string);
        digest.update(data);
        return digest.digest();
    }
}
