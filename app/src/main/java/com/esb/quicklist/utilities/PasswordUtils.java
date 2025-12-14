package com.esb.quicklist.utilities;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.EditText;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputLayout;
import java.util.regex.Pattern;

public class PasswordUtils {

    // Password validation pattern
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         // at least 1 digit
                    "(?=.*[a-zA-Z])" +      // any letter
                    "(?=.*[@#$%^&+=])" +    // at least 1 special character
                    "(?=\\S+$)" +           // no white spaces
                    ".{8,}" +               // at least 8 characters
                    "$");

    // Private constructor to prevent instantiation
    private PasswordUtils() {}

    // ==================== VALIDATION METHODS ====================

    // 1. Validate email format
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // 2. Validate password format
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) &&
                PASSWORD_PATTERN.matcher(password).matches();
    }

    // 3. Get password error message
    public static String getPasswordErrorMessage(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Password is required";
        }

        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }

        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) hasSpecial = true;
        }

        if (!hasLetter) return "Password must contain at least one letter";
        if (!hasDigit) return "Password must contain at least one number";
        if (!hasSpecial) return "Password must contain at least one symbol (@#$%^&+=)";

        return "Invalid password format";
    }

    // 4. Get password requirements text
    public static String getPasswordRequirements(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Password must contain: 8+ chars, 1 number, 1 symbol";
        }

        StringBuilder requirements = new StringBuilder();
        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        boolean hasLength = password.length() >= 8;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) hasSpecial = true;
        }

        requirements.append("Password must contain: ");
        if (!hasLength) requirements.append("8+ chars, ");
        if (!hasLetter) requirements.append("1 letter, ");
        if (!hasDigit) requirements.append("1 number, ");
        if (!hasSpecial) requirements.append("1 symbol, ");

        String reqText = requirements.toString();
        if (reqText.endsWith(", ")) {
            reqText = reqText.substring(0, reqText.length() - 2);
        }

        return reqText;
    }

    // 5. Check if password meets all requirements
    public static boolean meetsAllRequirements(String password) {
        if (TextUtils.isEmpty(password) || password.length() < 8) {
            return false;
        }

        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) hasSpecial = true;
        }

        return hasLetter && hasDigit && hasSpecial;
    }

    // 6. Get allowed symbols
    public static String getAllowedSymbols() {
        return "@#$%^&+=";
    }

    // 7. Get password requirements list
    public static String getRequirementsList() {
        return "Password Requirements:\n" +
                "• 8+ characters\n" +
                "• At least 1 letter\n" +
                "• At least 1 number\n" +
                "• At least 1 symbol (" + getAllowedSymbols() + ")";
    }

    // 8. Check if passwords match
    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    // ==================== TEXT WATCHER METHODS ====================

    // 9. Setup password strength validator
    public static void setupPasswordStrengthValidator(EditText passwordEditText,
                                                      TextInputLayout passwordInputLayout) {
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();
                String requirements = getPasswordRequirements(password);
                passwordInputLayout.setHelperText(requirements);

                // Change helper text color based on validation
                if (isValidPassword(password)) {
                    passwordInputLayout.setHelperTextColor(
                            ContextCompat.getColorStateList(passwordEditText.getContext(),
                                    android.R.color.holo_green_dark)
                    );
                } else {
                    passwordInputLayout.setHelperTextColor(
                            ContextCompat.getColorStateList(passwordEditText.getContext(),
                                    android.R.color.holo_red_dark)
                    );
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // 10. Setup simple password validator (just color change)
    public static void setupPasswordValidator(EditText passwordEditText,
                                              TextInputLayout passwordInputLayout) {
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();
                if (isValidPassword(password)) {
                    passwordInputLayout.setHelperTextColor(
                            ContextCompat.getColorStateList(passwordEditText.getContext(),
                                    android.R.color.holo_green_dark)
                    );
                } else {
                    passwordInputLayout.setHelperTextColor(
                            ContextCompat.getColorStateList(passwordEditText.getContext(),
                                    android.R.color.holo_red_dark)
                    );
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // ==================== FORM VALIDATION METHODS ====================

    // 11. Validate login form
    public static boolean validateLoginForm(String email, String password,
                                            TextInputLayout emailInputLayout,
                                            TextInputLayout passwordInputLayout) {
        boolean isValid = true;

        // Reset errors
        if (emailInputLayout != null) emailInputLayout.setError(null);
        if (passwordInputLayout != null) passwordInputLayout.setError(null);

        // Email validation
        if (TextUtils.isEmpty(email)) {
            if (emailInputLayout != null) emailInputLayout.setError("Email is required");
            isValid = false;
        } else if (!isValidEmail(email)) {
            if (emailInputLayout != null) emailInputLayout.setError("Please enter a valid email");
            isValid = false;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            if (passwordInputLayout != null) passwordInputLayout.setError("Password is required");
            isValid = false;
        } else if (!isValidPassword(password)) {
            if (passwordInputLayout != null) passwordInputLayout.setError(getPasswordErrorMessage(password));
            isValid = false;
        }

        return isValid;
    }

    // 12. Validate signup form
    public static boolean validateSignupForm(String email, String password, String confirmPassword,
                                             TextInputLayout emailInputLayout,
                                             TextInputLayout passwordInputLayout,
                                             TextInputLayout confirmPasswordInputLayout) {
        boolean isValid = true;

        // Reset errors
        if (emailInputLayout != null) emailInputLayout.setError(null);
        if (passwordInputLayout != null) passwordInputLayout.setError(null);
        if (confirmPasswordInputLayout != null) confirmPasswordInputLayout.setError(null);

        // Email validation
        if (TextUtils.isEmpty(email)) {
            if (emailInputLayout != null) emailInputLayout.setError("Email is required");
            isValid = false;
        } else if (!isValidEmail(email)) {
            if (emailInputLayout != null) emailInputLayout.setError("Please enter a valid email");
            isValid = false;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            if (passwordInputLayout != null) passwordInputLayout.setError("Password is required");
            isValid = false;
        } else if (!isValidPassword(password)) {
            if (passwordInputLayout != null) passwordInputLayout.setError(getPasswordErrorMessage(password));
            isValid = false;
        }

        // Confirm password validation
        if (TextUtils.isEmpty(confirmPassword)) {
            if (confirmPasswordInputLayout != null) confirmPasswordInputLayout.setError("Please confirm your password");
            isValid = false;
        } else if (!passwordsMatch(password, confirmPassword)) {
            if (confirmPasswordInputLayout != null) confirmPasswordInputLayout.setError("Passwords do not match");
            isValid = false;
        }

        return isValid;
    }

    // ==================== HELPER METHODS ====================

    // 13. Generate a strong password suggestion
    public static String generateStrongPassword() {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        String symbols = "@#$%^&+=";

        StringBuilder password = new StringBuilder();

        // Add at least one of each required character type
        password.append(letters.charAt((int)(Math.random() * letters.length())));
        password.append(numbers.charAt((int)(Math.random() * numbers.length())));
        password.append(symbols.charAt((int)(Math.random() * symbols.length())));

        // Fill remaining characters to reach minimum length
        String allChars = letters + numbers + symbols;
        while (password.length() < 8) {
            password.append(allChars.charAt((int)(Math.random() * allChars.length())));
        }

        // Shuffle the password
        return shuffleString(password.toString());
    }

    // 14. Shuffle a string
    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            int randomIndex = (int)(Math.random() * characters.length);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }
        return new String(characters);
    }

    // 15. Check password strength score (0-100)
    public static int getPasswordStrengthScore(String password) {
        if (TextUtils.isEmpty(password)) return 0;

        int score = 0;

        // Length score
        if (password.length() >= 8) score += 25;
        if (password.length() >= 12) score += 10;

        // Character variety score
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) hasSpecial = true;
        }

        if (hasUpper && hasLower) score += 25;
        if (hasDigit) score += 20;
        if (hasSpecial) score += 20;

        return Math.min(score, 100);
    }

    // 16. Get password strength description
    public static String getPasswordStrengthDescription(String password) {
        int score = getPasswordStrengthScore(password);

        if (score >= 80) return "Strong";
        if (score >= 60) return "Good";
        if (score >= 40) return "Fair";
        if (score >= 20) return "Weak";
        return "Very Weak";
    }
}