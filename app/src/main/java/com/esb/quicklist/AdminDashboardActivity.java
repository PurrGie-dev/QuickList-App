package com.esb.quicklist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView welcomeText;
    private TextView adminInfoText;
    private Button createListBtn;
    private Button manageListsBtn;
    private Button viewListsBtn;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        authManager = new AuthManager(this);

        // Check if user is admin
        if (!authManager.isCurrentUserAdmin()) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupListeners();
        displayAdminInfo();
    }

    private void initializeViews() {
        // Make sure these IDs match your activity_admin_dashboard.xml
        welcomeText = findViewById(R.id.welcomeText);
        adminInfoText = findViewById(R.id.adminInfoText); // This should exist in XML
        createListBtn = findViewById(R.id.createListBtn); // This should exist in XML
        manageListsBtn = findViewById(R.id.manageListsBtn); // This should exist in XML
        viewListsBtn = findViewById(R.id.viewListsBtn); // This should exist in XML
    }

    private void setupListeners() {
        createListBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateListActivity.class);
            startActivity(intent);
        });

        manageListsBtn.setOnClickListener(v -> {
            if (authManager.getCreatedShoppingLists().isEmpty()) {
                Toast.makeText(this, "You haven't created any lists yet", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, ManageListsActivity.class);
                startActivity(intent);
            }
        });

        viewListsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewListsActivity.class);
            startActivity(intent);
        });
    }

    private void displayAdminInfo() {
        String currentUser = authManager.getCurrentUser();
        int createdListsCount = authManager.getCreatedShoppingLists().size();
        int joinedListsCount = authManager.getUserShoppingLists().size();

        welcomeText.setText("Admin Dashboard\nWelcome, " + currentUser + "!");

        String infoText = "You are an Admin\n\n" +
                "Created Lists: " + createdListsCount + "\n" +
                "Joined Lists: " + joinedListsCount + "\n\n" +
                "As an admin, you can create lists and manage members.";

        adminInfoText.setText(infoText);
    }
}