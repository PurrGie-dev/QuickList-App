package com.esb.quicklist.management;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.esb.quicklist.utilities.AuthManager;
import com.esb.quicklist.R;
import com.esb.quicklist.models.ShoppingList;

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
                        .append("\n\n");
            }
        }

        listsText.setText(sb.toString());

        // Make the TextView clickable to show a selection dialog
        listsText.setOnClickListener(v -> {
            if (!createdLists.isEmpty()) {
                showListSelectionDialog(createdLists);
            }
        });
    }

    private void showListSelectionDialog(List<ShoppingList> lists) {
        String[] listNames = new String[lists.size()];
        for (int i = 0; i < lists.size(); i++) {
            listNames[i] = (i + 1) + ". " + lists.get(i).getListName() +
                    " (" + lists.get(i).getListCode() + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Select List to Manage")
                .setItems(listNames, (dialog, which) -> {
                    Intent intent = new Intent(ManageListsActivity.this, ManageListActivity.class);
                    intent.putExtra("LIST_CODE", lists.get(which).getListCode());
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}