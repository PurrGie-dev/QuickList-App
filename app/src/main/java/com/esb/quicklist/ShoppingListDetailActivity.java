package com.esb.quicklist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class ShoppingListDetailActivity extends AppCompatActivity {

    private TextView listNameText;
    private TextView listCodeText;
    private LinearLayout productsLayout;
    private Button addItemButton;
    private Button adminButton;
    private Button backButton;
    private AuthManager authManager;
    private String currentListCode;
    private final List<Product> productList = new ArrayList<>(); // Made final

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_detail);

        authManager = new AuthManager(this);
        currentListCode = getIntent().getStringExtra("LIST_CODE");

        if (currentListCode == null) {
            Toast.makeText(this, "Error: No list selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupListeners();
        loadListData();
    }

    private void initializeViews() {
        try {
            listNameText = findViewById(R.id.listNameText);
            listCodeText = findViewById(R.id.listCodeText);
            productsLayout = findViewById(R.id.productsLayout);
            addItemButton = findViewById(R.id.addItemButton);
            adminButton = findViewById(R.id.adminButton);
            backButton = findViewById(R.id.backButton);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupListeners() {
        addItemButton.setOnClickListener(v -> showAddItemDialog());

        adminButton.setOnClickListener(v -> {
            if (authManager.isListCreator(currentListCode) || authManager.isCurrentUserAdmin()) {
                showAdminOptions();
            } else {
                Toast.makeText(this, "Admin privileges required", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

    private void loadListData() {
        try {
            ShoppingList list = authManager.getShoppingList(currentListCode);
            if (list != null) {
                listNameText.setText(list.getListName());
                listCodeText.setText(String.format("Code: %s", list.getListCode()));

                boolean isCreator = authManager.isListCreator(currentListCode);
                boolean isAdmin = authManager.isCurrentUserAdmin();
                adminButton.setVisibility(isCreator || isAdmin ? View.VISIBLE : View.GONE);

                // Display members count
                TextView membersText = new TextView(this);
                membersText.setText("Members: " + list.getMemberCount());
                membersText.setPadding(0, 10, 0, 10);
                productsLayout.addView(membersText);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddItemDialog() {
        Toast.makeText(this, "Add item feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void showAdminOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Admin Options");

        String[] options = {"Delete Users", "List Settings"};

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showDeleteUsersDialog();
            } else if (which == 1) {
                openListSettings();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteUsersDialog() {
        ShoppingList list = authManager.getShoppingList(currentListCode);
        if (list == null) return;

        String creator = list.getCreatorEmail();
        StringBuilder message = new StringBuilder();
        message.append("Creator: ").append(creator).append("\n\n");
        message.append("Other members:\n");

        int memberCount = 0;
        for (String member : list.getMemberEmails()) {
            if (!member.equals(creator)) {
                message.append("- ").append(member).append("\n");
                memberCount++;
            }
        }

        if (memberCount == 0) {
            message.append("No other members to delete.");
        }

        new AlertDialog.Builder(this)
                .setTitle("List Members")
                .setMessage(message.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void openListSettings() {
        Intent intent = new Intent(this, ManageListActivity.class);
        intent.putExtra("LIST_CODE", currentListCode);
        startActivity(intent);
    }
}