package com.esb.quicklist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.esb.quicklist.utilities.AuthManager;
import com.esb.quicklist.ForgotPasswordActivity;
import com.esb.quicklist.utilities.PasswordUtils;
import com.esb.quicklist.R;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout, passwordInputLayout;
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView signUpTextView, forgotPasswordTextView;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize AuthManager
        authManager = new AuthManager(this);

        // Check if user is already logged in
        if (authManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.signUpTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        // Set initial helper text using PasswordUtils
        passwordInputLayout.setHelperText(PasswordUtils.getRequirementsList());

        // Setup password strength validator using PasswordUtils
        PasswordUtils.setupPasswordStrengthValidator(passwordEditText, passwordInputLayout);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());

        signUpTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Use PasswordUtils to validate the entire form
        boolean isValid = PasswordUtils.validateLoginForm(email, password,
                emailInputLayout, passwordInputLayout);

        if (isValid) {
            performLogin(email, password);
        }
    }

    private void performLogin(String email, String password) {
        showProgress(true);

        // Simulate network delay
        new Handler().postDelayed(() -> {
            showProgress(false);

            // REAL AUTHENTICATION CHECK
            if (authManager.loginUser(email, password)) {
                Toast.makeText(this, "Login successful! Welcome " + email, Toast.LENGTH_SHORT).show();
                navigateToMain();
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        }, 1500);
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showProgress(boolean show) {
        loginButton.setEnabled(!show);
        loginButton.setText(show ? "Logging in..." : "Login");
    }
}