package com.esb.quicklist.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.esb.quicklist.utilities.AuthManager;
import com.esb.quicklist.R;
import com.google.android.material.textfield.TextInputLayout;

public class CreateListActivity extends AppCompatActivity {

    private TextInputLayout listNameInputLayout;
    private EditText listNameEditText;
    private Button createListButton;
    private TextView generatedCodeText;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_list);

        authManager = new AuthManager(this);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        listNameInputLayout = findViewById(R.id.listNameInputLayout);
        listNameEditText = findViewById(R.id.listNameEditText);
        createListButton = findViewById(R.id.createListButton);
        generatedCodeText = findViewById(R.id.generatedCodeText);
    }

    private void setupListeners() {
        createListButton.setOnClickListener(v -> attemptCreateList());
    }

    private void attemptCreateList() {
        String listName = listNameEditText.getText().toString().trim();

        if (listName.isEmpty()) {
            listNameInputLayout.setError("Please enter a list name");
            return;
        }

        // Create the shopping list
        String listCode = authManager.createShoppingList(listName);
        if (listCode != null) {
            generatedCodeText.setVisibility(View.VISIBLE);
            generatedCodeText.setText("✅ List created successfully!\n\n" +
                    "📋 List Name: " + listName + "\n" +
                    "🔑 List Code: " + listCode + "\n\n" +
                    "Share this code with others to join your list!");
            createListButton.setEnabled(false);
            createListButton.setText("List Created");
            createListButton.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.darker_gray));

            // Show toast with instructions
            Toast.makeText(this, "List '" + listName + "' created! Code: " + listCode,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to create list", Toast.LENGTH_SHORT).show();
        }
    }
}