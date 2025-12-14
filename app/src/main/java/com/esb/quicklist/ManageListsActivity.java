package com.esb.quicklist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class ManageListsActivity extends AppCompatActivity {

    private TextView listsText;
    private Button backButton;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_lists);

        authManager = new AuthManager(this);

        initializeViews();
        setupListeners();
        displayCreatedLists();
    }

    private void initializeViews() {
        listsText = findViewById(R.id.listsText);
        backButton = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
    }

    private void displayCreatedLists() {
        StringBuilder sb = new StringBuilder("Lists You Created:\n\n");

        List<ShoppingList> createdLists = authManager.getCreatedShoppingLists();

        if (createdLists.isEmpty()) {
            sb.append("You haven't created any lists yet.\n");
            sb.append("Create a list first to manage it!");
        } else {
            int index = 1;
            for (ShoppingList list : createdLists) {
                sb.append(index++).append(". ").append(list.getListName())
                        .append("\nCode: ").append(list.getListCode())
                        .append("\nMembers: ").append(list.getMemberCount())
                        .append("\n");

                // Add action buttons as clickable text
                sb.append("[Manage] - [View Members] - [Share Code]\n\n");
            }
        }

        listsText.setText(sb.toString());
    }
}