package com.esb.quicklist.management;

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

public class ManageUsersActivity extends AppCompatActivity {

    private TextView usersText;
    private Button backButton;
    private AuthManager authManager;
    private String currentListCode;
    private ShoppingList currentList;

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

        currentList = authManager.getShoppingList(currentListCode);
        if (currentList == null) {
            Toast.makeText(this, "List not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupListeners();
        displayUsers();
    }

    private void initializeViews() {
        usersText = findViewById(R.id.usersText);
        backButton = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        // Make the text clickable to show remove options
        usersText.setOnClickListener(v -> showRemoveOptions());
    }

    private void displayUsers() {
        StringBuilder sb = new StringBuilder();
        sb.append("👑 List Creator:\n");

        List<String> members = authManager.getListMembers(currentListCode);

        // Show creator first
        for (String member : members) {
            if (currentList.isCreator(member)) {
                sb.append("• ").append(member).append(" (Creator)\n\n");
                break;
            }
        }

        sb.append("👤 Members:\n");
        boolean hasMembers = false;

        for (String member : members) {
            if (!currentList.isCreator(member)) {
                sb.append("• ").append(member).append("\n");
                hasMembers = true;
            }
        }

        if (!hasMembers) {
            sb.append("No other members\n");
        }

        sb.append("\n📝 Tap here to remove members");

        usersText.setText(sb.toString());
    }

    private void showRemoveOptions() {
        List<String> members = authManager.getListMembers(currentListCode);
        String[] memberArray = new String[members.size()];

        for (int i = 0; i < members.size(); i++) {
            String member = members.get(i);
            if (currentList.isCreator(member)) {
                memberArray[i] = member + " 👑 (Creator)";
            } else if (member.equals(authManager.getCurrentUser())) {
                memberArray[i] = member + " 👤 (You)";
            } else {
                memberArray[i] = member + " 👤 (Member)";
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Remove a Member")
                .setItems(memberArray, (dialog, which) -> {
                    String selectedMember = members.get(which);

                    // Check if we can remove this member
                    if (currentList.isCreator(selectedMember)) {
                        Toast.makeText(this, "Cannot remove the creator", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedMember.equals(authManager.getCurrentUser())) {
                        Toast.makeText(this, "Cannot remove yourself", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Confirm removal
                    confirmRemoveMember(selectedMember);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmRemoveMember(String memberEmail) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Member")
                .setMessage("Remove \"" + memberEmail + "\"?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    if (authManager.removeMemberFromList(currentListCode, memberEmail)) {
                        Toast.makeText(this, "Removed: " + memberEmail, Toast.LENGTH_SHORT).show();
                        displayUsers(); // Refresh
                    } else {
                        Toast.makeText(this, "Failed to remove", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}