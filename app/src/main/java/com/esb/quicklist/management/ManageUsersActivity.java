package com.esb.quicklist.management;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.esb.quicklist.utilities.AuthManager;
import com.esb.quicklist.R;

public class ManageUsersActivity extends AppCompatActivity {

    private TextView usersText;
    private Button backButton;
    private AuthManager authManager;
    private String currentListCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        authManager = new AuthManager(this);
        currentListCode = getIntent().getStringExtra("LIST_CODE");

        if (currentListCode == null) {
            Toast.makeText(this, "No list selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupListeners();
        displayUsersInfo();
    }

    private void initializeViews() {
        usersText = findViewById(R.id.usersText);
        backButton = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
    }

    private void displayUsersInfo() {
        StringBuilder sb = new StringBuilder("Users Management\n\n");
        sb.append("List Code: ").append(currentListCode).append("\n\n");
        sb.append("TODO: Implement user management\n");
        sb.append("Features to add:\n");
        sb.append("• View all list members\n");
        sb.append("• Remove users from list\n");
        sb.append("• Block users from list\n");

        usersText.setText(sb.toString());
    }
}