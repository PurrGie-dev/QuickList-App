package com.esb.quicklist.activities;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.esb.quicklist.R;
import com.esb.quicklist.utilities.PasswordUtils;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout;
    private EditText emailEditText;
    private Button resetButton;
    private TextView backToLoginTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        emailInputLayout = findViewById(R.id.forgotEmailInputLayout);
        emailEditText = findViewById(R.id.forgotEmailEditText);
        resetButton = findViewById(R.id.resetButton);
        backToLoginTextView = findViewById(R.id.backToLoginTextView);
    }

    private void setupListeners() {
        resetButton.setOnClickListener(v -> attemptReset());

        backToLoginTextView.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }

    private void attemptReset() {
        String email = emailEditText.getText().toString().trim();

        // Reset error
        emailInputLayout.setError(null);

        boolean isValid = true;

        // Email validation using PasswordUtils
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Email is required");
            isValid = false;
        } else if (!PasswordUtils.isValidEmail(email)) {  // USING PasswordUtils
            emailInputLayout.setError("Please enter a valid email");
            isValid = false;
        }

        if (isValid) {
            performReset(email);
        }
    }

    private void performReset(String email) {
        // TODO: Implement actual password reset logic
        showProgress(true);

        new Handler().postDelayed(() -> {
            showProgress(false);
            Toast.makeText(this, "Reset link sent to " + email, Toast.LENGTH_SHORT).show();
            finish(); // Go back to login
        }, 1500);
    }

    private void showProgress(boolean show) {
        resetButton.setEnabled(!show);
        resetButton.setText(show ? "Sending..." : "Send Reset Link");
    }
}