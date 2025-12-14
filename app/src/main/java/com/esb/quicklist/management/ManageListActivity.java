package com.esb.quicklist.management;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.esb.quicklist.utilities.AuthManager;
import com.esb.quicklist.utilities.ProductManager;
import com.esb.quicklist.R;
import com.esb.quicklist.models.ShoppingList;
import com.esb.quicklist.activities.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ManageListActivity extends AppCompatActivity {

    private TextView listNameText;
    private TextView listCodeText;
    private TextView membersText;
    private LinearLayout actionButtonsLayout;
    private Button deleteListBtn;
    private Button updateListBtn;
    private Button removeMemberBtn;
    private Button backButton;
    private Button copyCodeBtn;
    private AuthManager authManager;
    private ProductManager productManager;
    private String currentListCode;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_list);

        authManager = new AuthManager(this);
        productManager = new ProductManager(this);
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
        deleteListBtn = findViewById(R.id.deleteListBtn);
        updateListBtn = findViewById(R.id.updateListBtn);
        removeMemberBtn = findViewById(R.id.removeMemberBtn);
        backButton = findViewById(R.id.backButton);
        copyCodeBtn = findViewById(R.id.copyCodeBtn);
    }

    private void setupListeners() {
        deleteListBtn.setOnClickListener(v -> confirmDeleteList());
        updateListBtn.setOnClickListener(v -> showUpdateListDialog());
        removeMemberBtn.setOnClickListener(v -> showRemoveMemberDialog());
        backButton.setOnClickListener(v -> finish());
        copyCodeBtn.setOnClickListener(v -> copyListCodeToClipboard());
        membersText.setOnClickListener(v -> showRemoveMemberDialog());
    }

    private void copyListCodeToClipboard() {
        ShoppingList list = authManager.getShoppingList(currentListCode);
        if (list == null) return;

        String listCode = list.getListCode();

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("List Code", listCode);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "✓ Copied: " + listCode, Toast.LENGTH_SHORT).show();
    }

    private void showRemoveMemberDialog() {
        ShoppingList list = authManager.getShoppingList(currentListCode);
        if (list == null) return;

        List<String> members = authManager.getListMembers(currentListCode);

        String[] memberOptions = new String[members.size()];

        for (int i = 0; i < members.size(); i++) {
            String member = members.get(i);
            boolean isCreator = list.isCreator(member);
            boolean isCurrentUser = member.equals(authManager.getCurrentUser());

            if (isCreator) {
                memberOptions[i] = member + " 👑 (Creator - cannot remove)";
            } else if (isCurrentUser) {
                memberOptions[i] = member + " 👤 (You - cannot remove)";
            } else {
                memberOptions[i] = member + " 👤 (Tap to remove)";
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Remove Member from List")
                .setItems(memberOptions, (dialog, which) -> {
                    String selectedMember = members.get(which);

                    if (list.isCreator(selectedMember)) {
                        Toast.makeText(this, "Cannot remove the list creator", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedMember.equals(authManager.getCurrentUser())) {
                        Toast.makeText(this, "Cannot remove yourself", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    confirmRemoveMember(selectedMember);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmRemoveMember(String memberEmail) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Member")
                .setMessage("Remove \"" + memberEmail + "\" from this list?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    if (authManager.removeMemberFromList(currentListCode, memberEmail)) {
                        Toast.makeText(this, "✓ Removed: " + memberEmail, Toast.LENGTH_SHORT).show();
                        displayListInfo();
                    } else {
                        Toast.makeText(this, "Failed to remove member", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteList() {
        ShoppingList list = authManager.getShoppingList(currentListCode);
        if (list == null) return;

        int productCount = productManager.getProductsForList(currentListCode).size();

        String message = "⚠️ DELETE LIST:\n\n" +
                "List: " + list.getListName() + "\n" +
                "Code: " + list.getListCode() + "\n" +
                "Products: " + productCount + " item(s)\n" +
                "Members: " + list.getMemberCount() + " person(s)\n\n" +
                "This will permanently delete everything!\n\n" +
                "Type \"DELETE\" to confirm:";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚠️ Delete List");
        builder.setMessage(message);

        final EditText input = new EditText(this);
        input.setHint("Type DELETE here");
        builder.setView(input);

        builder.setPositiveButton("Delete", null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String confirmationText = input.getText().toString().trim();
            if (!confirmationText.equals("DELETE")) {
                input.setError("Type DELETE exactly as shown");
                return;
            }
            dialog.dismiss();
            deleteList();
        });
    }

    private void deleteList() {
        // Show loading
        deleteListBtn.setEnabled(false);
        deleteListBtn.setText("Deleting...");

        // Get list info
        ShoppingList list = authManager.getShoppingList(currentListCode);
        String listName = list != null ? list.getListName() : "List";

        // Delete list from storage
        if (deleteListFromStorage()) {
            Toast.makeText(this, "✓ Deleted list: " + listName, Toast.LENGTH_SHORT).show();

            // Go back to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to delete list", Toast.LENGTH_SHORT).show();
            deleteListBtn.setEnabled(true);
            deleteListBtn.setText("Delete List");
        }
    }

    private boolean deleteListFromStorage() {
        try {
            // Get SharedPreferences
            sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

            // 1. Delete list from shopping_lists
            String shoppingListsJson = sharedPreferences.getString("shopping_lists", "{}");
            JSONObject allLists = new JSONObject(shoppingListsJson);

            if (allLists.has(currentListCode)) {
                allLists.remove(currentListCode);
                sharedPreferences.edit()
                        .putString("shopping_lists", allLists.toString())
                        .apply();
            }

            // 2. Delete list from all users' joined/created lists
            removeListFromAllUsers();

            // 3. Delete all products in this list
            deleteAllProductsInList();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void removeListFromAllUsers() {
        try {
            String usersJson = sharedPreferences.getString("registered_users", "[]");
            JSONArray usersArray = new JSONArray(usersJson);

            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject userObject = usersArray.getJSONObject(i);

                // Remove from joinedLists
                if (userObject.has("joinedLists")) {
                    String joinedLists = userObject.getString("joinedLists");
                    if (joinedLists.contains(currentListCode)) {
                        String newJoinedLists = removeListFromString(joinedLists, currentListCode);
                        userObject.put("joinedLists", newJoinedLists);
                    }
                }

                // Remove from createdLists
                if (userObject.has("createdLists")) {
                    String createdLists = userObject.getString("createdLists");
                    if (createdLists.contains(currentListCode)) {
                        String newCreatedLists = removeListFromString(createdLists, currentListCode);
                        userObject.put("createdLists", newCreatedLists);
                    }
                }
            }

            // Save updated users
            sharedPreferences.edit()
                    .putString("registered_users", usersArray.toString())
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String removeListFromString(String listString, String listToRemove) {
        if (listString == null || listString.isEmpty()) {
            return "";
        }

        String[] lists = listString.split(",");
        StringBuilder result = new StringBuilder();

        for (String list : lists) {
            if (!list.trim().equals(listToRemove) && !list.trim().isEmpty()) {
                if (result.length() > 0) {
                    result.append(",");
                }
                result.append(list.trim());
            }
        }

        return result.toString();
    }

    private void deleteAllProductsInList() {
        try {
            // Get products for this list
            List<com.esb.quicklist.models.Product> products = productManager.getProductsForList(currentListCode);

            // Delete each product
            for (com.esb.quicklist.models.Product product : products) {
                productManager.deleteProduct(product.getId());
            }
        } catch (Exception e) {
            // Continue even if product deletion fails
        }
    }

    private void showUpdateListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update List Name");

        final EditText input = new EditText(this);

        ShoppingList list = authManager.getShoppingList(currentListCode);
        if (list != null) {
            input.setText(list.getListName());
            input.setSelection(list.getListName().length());
        }

        input.setHint("Enter new list name");
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "List name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (list != null && newName.equals(list.getListName())) {
                Toast.makeText(this, "Name unchanged", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Confirm Update")
                    .setMessage("Change list name to \"" + newName + "\"?")
                    .setPositiveButton("Yes", (d, w) -> updateListName(newName))
                    .setNegativeButton("No", null)
                    .show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateListName(String newName) {
        updateListBtn.setEnabled(false);
        updateListBtn.setText("Updating...");

        if (authManager.updateShoppingList(currentListCode, newName)) {
            Toast.makeText(this, "✓ List name updated to: " + newName, Toast.LENGTH_SHORT).show();
            displayListInfo();
        } else {
            Toast.makeText(this, "Failed to update list name", Toast.LENGTH_SHORT).show();
        }

        updateListBtn.setEnabled(true);
        updateListBtn.setText("Update List Name");
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

            membersBuilder.append("\n👤 Tap here to remove members");
            membersText.setText(membersBuilder.toString());
        }
    }

    private void setupActionButtons() {
        boolean isCreator = authManager.isListCreator(currentListCode);

        deleteListBtn.setVisibility(isCreator ? android.view.View.VISIBLE : android.view.View.GONE);
        updateListBtn.setVisibility(isCreator ? android.view.View.VISIBLE : android.view.View.GONE);
        removeMemberBtn.setVisibility(isCreator ? android.view.View.VISIBLE : android.view.View.GONE);
        copyCodeBtn.setVisibility(android.view.View.VISIBLE);
    }
}