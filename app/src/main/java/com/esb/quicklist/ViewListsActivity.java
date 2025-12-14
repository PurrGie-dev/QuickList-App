package com.esb.quicklist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class ViewListsActivity extends AppCompatActivity {

    private TextView listsText;
    private Button backButton;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_lists);

        authManager = new AuthManager(this);

        initializeViews();
        setupListeners();
        displayLists();
    }

    private void initializeViews() {
        listsText = findViewById(R.id.listsText);
        backButton = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
    }

    private void displayLists() {
        StringBuilder sb = new StringBuilder("All Your Shopping Lists:\n\n");

        List<ShoppingList> userLists = authManager.getUserShoppingLists();

        if (userLists.isEmpty()) {
            sb.append("You haven't joined any lists yet.\n");
            sb.append("Create a list or join one using a code!");
        } else {
            for (ShoppingList list : userLists) {
                String creatorText = authManager.isListCreator(list.getListCode()) ?
                        " 👑 (You created this)" : " 👤 (Created by: " + list.getCreatorEmail() + ")";
                sb.append("📋 ").append(list.getListName())
                        .append("\nCode: ").append(list.getListCode())
                        .append(creatorText)
                        .append("\nMembers: ").append(list.getMemberCount())
                        .append("\nCategory: ").append(list.getCategory())
                        .append("\n\n");
            }
        }

        listsText.setText(sb.toString());
    }
}