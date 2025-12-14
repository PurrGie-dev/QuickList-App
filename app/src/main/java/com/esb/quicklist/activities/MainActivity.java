package com.esb.quicklist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.esb.quicklist.utilities.AuthManager;
import com.esb.quicklist.R;
import com.esb.quicklist.models.ShoppingList;
import com.esb.quicklist.models.User;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeText;
    private Button openButton;
    private Button addButton;
    private Button logoutButton;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authManager = new AuthManager(this);

        initializeViews();
        setupListeners();
        displayUserInfo();
    }

    private void initializeViews() {
        welcomeText = findViewById(R.id.welcomeText);
        openButton = findViewById(R.id.openButton);
        addButton = findViewById(R.id.addButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void setupListeners() {
        openButton.setOnClickListener(v -> showOpenOptionsDialog());

        addButton.setOnClickListener(v -> {
            // Go to Create List Activity
            Intent intent = new Intent(this, CreateListActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            authManager.logout();
            navigateToLogin();
        });
    }

    private void showOpenOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Open List");
        builder.setMessage("Choose an option:");

        builder.setPositiveButton("Open Existing List", (dialog, which) -> {
            openExistingLists();
        });

        builder.setNegativeButton("Join List via Code", (dialog, which) -> {
            showJoinListDialog();
        });

        builder.setNeutralButton("Cancel", null);

        builder.show();
    }

    private void openExistingLists() {
        // Check if user has any lists
        if (authManager.getUserShoppingLists().isEmpty()) {
            Toast.makeText(this, "You don't have any lists yet!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show dialog with user's lists
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a List to Open");

        // Get user's lists
        java.util.List<ShoppingList> userLists = authManager.getUserShoppingLists();
        String[] listNames = new String[userLists.size()];
        for (int i = 0; i < userLists.size(); i++) {
            String creatorText = authManager.isListCreator(userLists.get(i).getListCode()) ?
                    " 👑" : " 👤";
            listNames[i] = userLists.get(i).getListName() + creatorText;
        }

        builder.setItems(listNames, (dialog, which) -> {
            ShoppingList selectedList = userLists.get(which);
            openShoppingListDetails(selectedList.getListCode());
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void openShoppingListDetails(String listCode) {
        // TODO: Create a new activity for viewing/editing shopping list with checkable items
        // For now, we'll show a toast
        ShoppingList list = authManager.getShoppingList(listCode);
        if (list != null) {
            // Create intent to open ShoppingListDetailActivity
            Intent intent = new Intent(this, ShoppingListDetailActivity.class);
            intent.putExtra("LIST_CODE", listCode);
            startActivity(intent);
        }
    }

    private void showJoinListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Join List via Code");
        builder.setMessage("Enter the 6-character list code:");

        // Create input field
        final EditText input = new EditText(this);
        input.setHint("e.g., ABC123");
        input.setInputType(android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        builder.setView(input);

        builder.setPositiveButton("Join", (dialog, which) -> {
            String code = input.getText().toString().trim().toUpperCase();
            joinListWithCode(code);
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void joinListWithCode(String code) {
        if (code.isEmpty() || code.length() != 6) {
            Toast.makeText(this, "Please enter a valid 6-character code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Attempt to join
        if (authManager.joinShoppingList(code)) {
            ShoppingList list = authManager.getShoppingList(code);
            if (list != null) {
                Toast.makeText(this, "Successfully joined: " + list.getListName(), Toast.LENGTH_SHORT).show();
                // Optionally open the list immediately
                openShoppingListDetails(code);
            }
        } else {
            Toast.makeText(this, "Invalid code or already a member", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayUserInfo() {
        User user = authManager.getCurrentUserObject();
        if (user != null) {
            String roleText = user.isAdmin() ? " (Admin)" : " (User)";
            welcomeText.setText("Welcome, " + user.getEmail() + roleText + "!");
        } else {
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}