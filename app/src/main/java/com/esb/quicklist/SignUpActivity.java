package com.esb.quicklist;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout, passwordInputLayout, confirmPasswordInputLayout;
    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signUpButton;
    private TextView loginTextView;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize AuthManager
        authManager = new AuthManager(this);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        emailInputLayout = findViewById(R.id.signupEmailInputLayout);
        passwordInputLayout = findViewById(R.id.signupPasswordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.signupConfirmPasswordInputLayout);
        emailEditText = findViewById(R.id.signupEmailEditText);
        passwordEditText = findViewById(R.id.signupPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.signupConfirmPasswordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        loginTextView = findViewById(R.id.loginTextView);

        // Set initial helper text using PasswordUtils
        passwordInputLayout.setHelperText(PasswordUtils.getRequirementsList());

        // Setup password validator using PasswordUtils
        PasswordUtils.setupPasswordValidator(passwordEditText, passwordInputLayout);
    }

    private void setupListeners() {
        signUpButton.setOnClickListener(v -> attemptSignUp());

        loginTextView.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }

    private void attemptSignUp() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Use PasswordUtils to validate the entire form
        boolean isValid = PasswordUtils.validateSignupForm(email, password, confirmPassword,
                emailInputLayout, passwordInputLayout,
                confirmPasswordInputLayout);

        if (isValid) {
            performSignUp(email, password);
        }
    }

    private void performSignUp(String email, String password) {
        showProgress(true);

        new android.os.Handler().postDelayed(() -> {
            showProgress(false);

            // REAL REGISTRATION using AuthManager
            if (authManager.registerUser(email, password)) {
                Toast.makeText(this, "Account created for " + email, Toast.LENGTH_SHORT).show();
                finish(); // Go back to login
            } else {
                Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
            }
        }, 1500);
    }

    private void showProgress(boolean show) {
        signUpButton.setEnabled(!show);
        signUpButton.setText(show ? "Creating Account..." : "Sign Up");
    }
}