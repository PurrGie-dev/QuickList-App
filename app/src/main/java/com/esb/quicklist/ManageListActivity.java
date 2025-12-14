package com.esb.quicklist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class ManageListActivity extends AppCompatActivity {

    private TextView listNameText;
    private TextView listCodeText;
    private TextView membersText;
    private LinearLayout actionButtonsLayout;
    private Button manageProductsBtn;
    private Button manageCategoriesBtn;
    private Button manageUsersBtn;
    private Button deleteListBtn;
    private Button updateListBtn;
    private Button backButton;
    private AuthManager authManager;
    private String currentListCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_list);

        authManager = new AuthManager(this);
        currentListCode = getIntent().getStringExtra("LIST_CODE");

        if (currentListCode == null) {
            Toast.makeText(this, "No list selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupListeners();
        displayListInfo();
        setupActionButtons();
    }

    private void initializeViews() {
        listNameText = findViewById(R.id.listNameText);
        listCodeText = findViewById(R.id.listCodeText);
        membersText = findViewById(R.id.membersText);
        actionButtonsLayout = findViewById(R.id.actionButtonsLayout);
        manageProductsBtn = findViewById(R.id.manageProductsBtn);
        manageCategoriesBtn = findViewById(R.id.manageCategoriesBtn);
        manageUsersBtn = findViewById(R.id.manageUsersBtn);
        deleteListBtn = findViewById(R.id.deleteListBtn);
        updateListBtn = findViewById(R.id.updateListBtn);
        backButton = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        manageProductsBtn.setOnClickListener(v -> openProductsManager());
        manageCategoriesBtn.setOnClickListener(v -> openCategoriesManager());
        manageUsersBtn.setOnClickListener(v -> openUsersManager());
        deleteListBtn.setOnClickListener(v -> confirmDeleteList());
        updateListBtn.setOnClickListener(v -> showUpdateListDialog());
        backButton.setOnClickListener(v -> finish());
    }

    private void openProductsManager() {
        Intent intent = new Intent(this, ManageProductsActivity.class);
        intent.putExtra("LIST_CODE", currentListCode);
        startActivity(intent);
    }

    private void openCategoriesManager() {
        Intent intent = new Intent(this, ManageCategoriesActivity.class);
        intent.putExtra("LIST_CODE", currentListCode);
        startActivity(intent);
    }

    private void openUsersManager() {
        Intent intent = new Intent(this, ManageUsersActivity.class);
        intent.putExtra("LIST_CODE", currentListCode);
        startActivity(intent);
    }

    private void confirmDeleteList() {
        new AlertDialog.Builder(this)
                .setTitle("Delete List")
                .setMessage("Are you sure you want to delete this list? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteList())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteList() {
        // TODO: Implement list deletion
        Toast.makeText(this, "List deleted (not implemented)", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showUpdateListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update List Name");

        TextInputLayout inputLayout = new TextInputLayout(this);
        inputLayout.setPadding(50, 0, 50, 0);

        // TODO: Get current list name and set as default
        builder.setView(inputLayout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = inputLayout.getEditText().getText().toString();
            updateListName(newName);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateListName(String newName) {
        // TODO: Implement list name update
        Toast.makeText(this, "List name updated (not implemented)", Toast.LENGTH_SHORT).show();
        displayListInfo();
    }

    private void displayListInfo() {
        ShoppingList list = authManager.getShoppingList(currentListCode);
        if (list != null) {
            listNameText.setText("List: " + list.getListName());
            listCodeText.setText("Code: " + list.getListCode());

            StringBuilder membersBuilder = new StringBuilder("Members:\n\n");
            List<String> members = authManager.getListMembers(currentListCode);

            for (String memberEmail : members) {
                String creatorIndicator = list.isCreator(memberEmail) ? " 👑 (Creator)" : " 👤 (Member)";
                membersBuilder.append("• ").append(memberEmail).append(creatorIndicator).append("\n");
            }

            membersText.setText(membersBuilder.toString());
        }
    }

    private void setupActionButtons() {
        // Show/hide buttons based on user permissions
        boolean isCreator = authManager.isListCreator(currentListCode);

        manageProductsBtn.setVisibility(isCreator ? android.view.View.VISIBLE : android.view.View.GONE);
        manageCategoriesBtn.setVisibility(isCreator ? android.view.View.VISIBLE : android.view.View.GONE);
        manageUsersBtn.setVisibility(isCreator ? android.view.View.VISIBLE : android.view.View.GONE);
        deleteListBtn.setVisibility(isCreator ? android.view.View.VISIBLE : android.view.View.GONE);
        updateListBtn.setVisibility(isCreator ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}