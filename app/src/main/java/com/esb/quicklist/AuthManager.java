package com.esb.quicklist;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuthManager {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USERS = "registered_users";
    private static final String KEY_CURRENT_USER = "current_user";
    private static final String KEY_SHOPPING_LISTS = "shopping_lists";
    private static final String TAG = "AuthManager";

    private final SharedPreferences sharedPreferences;

    public AuthManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
// Add to AuthManager class

    // Product Management
    public boolean addProductToList(String listCode, Product product) {
        // Implementation
        return true;
    }

    public boolean removeProductFromList(String listCode, String productId) {
        // Implementation
        return true;
    }

    public boolean updateProductInList(String listCode, Product updatedProduct) {
        // Implementation
        return true;
    }

    // Category Management
    public boolean addCategoryToList(String listCode, Category category) {
        // Implementation
        return true;
    }

    public boolean removeCategoryFromList(String listCode, String categoryId) {
        // Implementation
        return true;
    }

    // User Management
    public boolean blockUserFromList(String listCode, String userEmail) {
        // Implementation
        return true;
    }

    public boolean unblockUserFromList(String listCode, String userEmail) {
        // Implementation
        return true;
    }
    public boolean registerUser(String email, String password) {
        return registerUser(email, password, User.UserRole.USER);
    }

    // Register a new user with specific role
    public boolean registerUser(String email, String password, User.UserRole role) {
        Log.d(TAG, "Attempting to register: " + email + " as " + role);

        // Check if user already exists
        if (userExists(email)) {
            Log.d(TAG, "User already exists: " + email);
            return false;
        }

        // Get existing users
        List<User> users = getRegisteredUsers();

        // Add new user
        users.add(new User(email, password, role));

        // Save to SharedPreferences
        saveUsers(users);

        Log.d(TAG, "Registration successful for: " + email + " as " + role);
        return true;
    }

    // Login user
    public boolean loginUser(String email, String password) {
        Log.d(TAG, "Attempting login for: " + email);

        List<User> users = getRegisteredUsers();

        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                // Save current user
                sharedPreferences.edit().putString(KEY_CURRENT_USER, email).apply();
                Log.d(TAG, "Login successful for: " + email);
                return true;
            }
        }

        Log.d(TAG, "Login failed for: " + email);
        return false;
    }

    // Create a new shopping list
    public String createShoppingList(String listName) {
        return createShoppingList(listName, "General");
    }

    public String createShoppingList(String listName, String category) {
        String currentUserEmail = getCurrentUser();
        if (currentUserEmail == null) {
            return null;
        }

        // Generate unique list code
        String listCode = generateListCode();

        // Create new shopping list
        ShoppingList shoppingList = new ShoppingList(listCode, listName, currentUserEmail, category);

        // Save shopping list
        saveShoppingList(shoppingList);

        // Add to user's created lists
        User user = getCurrentUserObject();
        if (user != null) {
            user.addCreatedList(listCode);
            user.addJoinedList(listCode); // Creator is automatically a member
            updateUser(user);
        }

        return listCode;
    }

    // Join a shopping list using list code
    public boolean joinShoppingList(String listCode) {
        String currentUserEmail = getCurrentUser();
        if (currentUserEmail == null) {
            return false;
        }

        // Check if list exists
        ShoppingList shoppingList = getShoppingList(listCode);
        if (shoppingList == null) {
            return false;
        }

        // Check if user is already a member
        if (shoppingList.isMember(currentUserEmail)) {
            return false;
        }

        // Add user to list members
        shoppingList.addMember(currentUserEmail);
        saveShoppingList(shoppingList);

        // Add list to user's joined lists
        User user = getCurrentUserObject();
        if (user != null) {
            user.addJoinedList(listCode);
            updateUser(user);
        }

        return true;
    }

    // Leave a shopping list
    public boolean leaveShoppingList(String listCode) {
        String currentUserEmail = getCurrentUser();
        if (currentUserEmail == null) {
            return false;
        }

        // Check if list exists
        ShoppingList shoppingList = getShoppingList(listCode);
        if (shoppingList == null) {
            return false;
        }

        // Check if user is the creator (creator cannot leave)
        if (shoppingList.isCreator(currentUserEmail)) {
            return false;
        }

        // Remove user from list members
        shoppingList.removeMember(currentUserEmail);
        saveShoppingList(shoppingList);

        // Remove list from user's joined lists
        User user = getCurrentUserObject();
        if (user != null) {
            user.removeJoinedList(listCode);
            updateUser(user);
        }

        return true;
    }

    // Remove member from shopping list (only creator can do this)
    public boolean removeMemberFromList(String listCode, String memberEmail) {
        String currentUserEmail = getCurrentUser();
        if (currentUserEmail == null) {
            return false;
        }

        // Check if list exists
        ShoppingList shoppingList = getShoppingList(listCode);
        if (shoppingList == null) {
            return false;
        }

        // Check if current user is the creator
        if (!shoppingList.isCreator(currentUserEmail)) {
            return false;
        }

        // Check if trying to remove creator
        if (shoppingList.isCreator(memberEmail)) {
            return false;
        }

        // Remove member from list
        shoppingList.removeMember(memberEmail);
        saveShoppingList(shoppingList);

        // Remove list from member's joined lists
        User member = getUserByEmail(memberEmail);
        if (member != null) {
            member.removeJoinedList(listCode);
            updateUser(member);
        }

        return true;
    }

    // Generate unique list code
    private String generateListCode() {
        String code;
        do {
            // Generate 6-character alphanumeric code
            code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (getShoppingList(code) != null);

        return code;
    }

    // Get shopping list by code
    public ShoppingList getShoppingList(String listCode) {
        String shoppingListsJson = sharedPreferences.getString(KEY_SHOPPING_LISTS, "{}");
        try {
            JSONObject jsonObject = new JSONObject(shoppingListsJson);
            if (jsonObject.has(listCode)) {
                JSONObject listJson = jsonObject.getJSONObject(listCode);

                String listName = listJson.getString("listName");
                String creatorEmail = listJson.getString("creatorEmail");
                String category = listJson.optString("category", "General");

                ShoppingList shoppingList = new ShoppingList(listCode, listName, creatorEmail, category);

                // Add members
                JSONArray membersArray = listJson.getJSONArray("members");
                for (int i = 0; i < membersArray.length(); i++) {
                    shoppingList.addMember(membersArray.getString(i));
                }

                return shoppingList;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting shopping list: " + e.getMessage());
        }
        return null;
    }

    // Save shopping list
    private void saveShoppingList(ShoppingList shoppingList) {
        String shoppingListsJson = sharedPreferences.getString(KEY_SHOPPING_LISTS, "{}");
        try {
            JSONObject jsonObject = new JSONObject(shoppingListsJson);

            JSONObject listJson = new JSONObject();
            listJson.put("listName", shoppingList.getListName());
            listJson.put("creatorEmail", shoppingList.getCreatorEmail());
            listJson.put("category", shoppingList.getCategory());

            JSONArray membersArray = new JSONArray();
            for (String member : shoppingList.getMemberEmails()) {
                membersArray.put(member);
            }
            listJson.put("members", membersArray);

            jsonObject.put(shoppingList.getListCode(), listJson);

            sharedPreferences.edit().putString(KEY_SHOPPING_LISTS, jsonObject.toString()).apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error saving shopping list: " + e.getMessage());
        }
    }

    // Get all shopping lists for current user (both created and joined)
    public List<ShoppingList> getUserShoppingLists() {
        List<ShoppingList> userLists = new ArrayList<>();
        String currentUserEmail = getCurrentUser();
        if (currentUserEmail == null) {
            return userLists;
        }

        String shoppingListsJson = sharedPreferences.getString(KEY_SHOPPING_LISTS, "{}");
        try {
            JSONObject jsonObject = new JSONObject(shoppingListsJson);

            for (String listCode : getUserJoinedLists()) {
                if (jsonObject.has(listCode)) {
                    JSONObject listJson = jsonObject.getJSONObject(listCode);

                    String listName = listJson.getString("listName");
                    String creatorEmail = listJson.getString("creatorEmail");
                    String category = listJson.optString("category", "General");

                    ShoppingList shoppingList = new ShoppingList(listCode, listName, creatorEmail, category);

                    // Add members
                    JSONArray membersArray = listJson.getJSONArray("members");
                    for (int i = 0; i < membersArray.length(); i++) {
                        shoppingList.addMember(membersArray.getString(i));
                    }

                    userLists.add(shoppingList);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting user shopping lists: " + e.getMessage());
        }

        return userLists;
    }

    // Get shopping lists created by current user
    public List<ShoppingList> getCreatedShoppingLists() {
        List<ShoppingList> createdLists = new ArrayList<>();
        User user = getCurrentUserObject();
        if (user == null || user.getCreatedLists().isEmpty()) {
            return createdLists;
        }

        String[] listCodes = user.getCreatedLists().split(",");
        for (String listCode : listCodes) {
            if (!listCode.trim().isEmpty()) {
                ShoppingList list = getShoppingList(listCode.trim());
                if (list != null) {
                    createdLists.add(list);
                }
            }
        }

        return createdLists;
    }

    // Get lists joined by current user
    public List<String> getUserJoinedLists() {
        User user = getCurrentUserObject();
        if (user == null || user.getJoinedLists().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> lists = new ArrayList<>();
        String[] codes = user.getJoinedLists().split(",");
        for (String code : codes) {
            lists.add(code.trim());
        }
        return lists;
    }

    // Get user by email
    private User getUserByEmail(String email) {
        List<User> users = getRegisteredUsers();
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    // Update user in storage
    private void updateUser(User updatedUser) {
        List<User> users = getRegisteredUsers();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmail().equals(updatedUser.getEmail())) {
                users.set(i, updatedUser);
                saveUsers(users);
                return;
            }
        }
    }

    // Get current user object
    public User getCurrentUserObject() {
        String email = getCurrentUser();
        if (email == null) return null;

        List<User> users = getRegisteredUsers();
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    // Check if current user is admin
    public boolean isCurrentUserAdmin() {
        User user = getCurrentUserObject();
        return user != null && user.isAdmin();
    }

    // Check if current user is creator of a list
    public boolean isListCreator(String listCode) {
        String currentUserEmail = getCurrentUser();
        if (currentUserEmail == null) return false;

        ShoppingList list = getShoppingList(listCode);
        return list != null && list.isCreator(currentUserEmail);
    }

    // Get list members
    public List<String> getListMembers(String listCode) {
        ShoppingList list = getShoppingList(listCode);
        if (list != null) {
            return list.getMemberEmails();
        }
        return new ArrayList<>();
    }

    // Existing helper methods
    private List<User> getRegisteredUsers() {
        String usersJson = sharedPreferences.getString(KEY_USERS, "[]");
        List<User> users = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(usersJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String email = jsonObject.getString("email");
                String password = jsonObject.getString("password");
                User.UserRole role = User.UserRole.valueOf(jsonObject.optString("role", "USER"));
                String createdLists = jsonObject.optString("createdLists", "");
                String joinedLists = jsonObject.optString("joinedLists", "");

                User user = new User(email, password, role);

                // Add created lists
                if (!createdLists.isEmpty()) {
                    String[] lists = createdLists.split(",");
                    for (String listCode : lists) {
                        if (!listCode.trim().isEmpty()) {
                            user.addCreatedList(listCode.trim());
                        }
                    }
                }

                // Add joined lists
                if (!joinedLists.isEmpty()) {
                    String[] lists = joinedLists.split(",");
                    for (String listCode : lists) {
                        if (!listCode.trim().isEmpty()) {
                            user.addJoinedList(listCode.trim());
                        }
                    }
                }

                users.add(user);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing users JSON: " + e.getMessage());
        }

        return users;
    }

    private void saveUsers(List<User> users) {
        JSONArray jsonArray = new JSONArray();

        try {
            for (User user : users) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("email", user.getEmail());
                jsonObject.put("password", user.getPassword());
                jsonObject.put("role", user.getRole().name());
                if (!user.getCreatedLists().isEmpty()) {
                    jsonObject.put("createdLists", user.getCreatedLists());
                }
                if (!user.getJoinedLists().isEmpty()) {
                    jsonObject.put("joinedLists", user.getJoinedLists());
                }
                jsonArray.put(jsonObject);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error creating users JSON: " + e.getMessage());
        }

        sharedPreferences.edit().putString(KEY_USERS, jsonArray.toString()).apply();
    }

    public String getCurrentUser() {
        return sharedPreferences.getString(KEY_CURRENT_USER, null);
    }

    public void logout() {
        sharedPreferences.edit().remove(KEY_CURRENT_USER).apply();
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public boolean userExists(String email) {
        List<User> users = getRegisteredUsers();
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    // DEBUG: Clear all data
    public void clearAllData() {
        sharedPreferences.edit().clear().apply();
        Log.d(TAG, "Cleared all data");
    }
}