package com.esb.quicklist.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.esb.quicklist.utilities.AuthManager;
import com.esb.quicklist.R;
import com.esb.quicklist.models.ShoppingList;
import com.google.android.material.textfield.TextInputLayout;

public class JoinListActivity extends AppCompatActivity {

    private TextInputLayout codeInputLayout;
    private EditText codeEditText;
    private Button joinButton;
    private TextView joinedListsText; // Changed from myListsText
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_list);

        authManager = new AuthManager(this);

        initializeViews();
        setupListeners();
        displayJoinedLists(); // Updated method name
    }

    private void initializeViews() {
        codeInputLayout = findViewById(R.id.codeInputLayout);
        codeEditText = findViewById(R.id.codeEditText);
        joinButton = findViewById(R.id.joinButton);
        joinedListsText = findViewById(R.id.joinedListsText); // Updated ID
    }

    private void setupListeners() {
        joinButton.setOnClickListener(v -> attemptJoinList());
    }

    private void attemptJoinList() {
        String code = codeEditText.getText().toString().trim().toUpperCase();

        if (code.isEmpty()) {
            codeInputLayout.setError("Please enter a list code");
            return;
        }

        // Check if list exists
        ShoppingList shoppingList = authManager.getShoppingList(code);
        if (shoppingList == null) {
            codeInputLayout.setError("Invalid list code");
            return;
        }

        // Attempt to join
        if (authManager.joinShoppingList(code)) {
            Toast.makeText(this, "Successfully joined shopping list: " + shoppingList.getListName(),
                    Toast.LENGTH_SHORT).show();
            codeEditText.setText("");
            codeInputLayout.setError(null);
            displayJoinedLists(); // Updated method name
        } else {
            Toast.makeText(this, "Failed to join list. You may have already joined.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void displayJoinedLists() { // Updated method name
        StringBuilder sb = new StringBuilder("My Shopping Lists:\n\n");

        for (ShoppingList list : authManager.getUserShoppingLists()) {
            String creatorText = authManager.isListCreator(list.getListCode()) ?
                    " (Creator)" : " (Member)";
            sb.append("• ").append(list.getListName())
                    .append(" - Code: ").append(list.getListCode())
                    .append(creatorText)
                    .append("\nMembers: ").append(list.getMemberCount())
                    .append("\n\n");
        }

        if (authManager.getUserShoppingLists().isEmpty()) {
            sb.append("You haven't joined any lists yet.\n");
            sb.append("Enter a list code above to join one!");
        }

        joinedListsText.setText(sb.toString()); // Updated variable name
    }
}