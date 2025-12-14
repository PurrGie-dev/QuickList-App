package com.esb.quicklist.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.esb.quicklist.models.Category;
import com.esb.quicklist.models.Product;
import com.esb.quicklist.models.ShoppingList;
import com.esb.quicklist.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AuthManager {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USERS = "registered_users";
    private static final String KEY_CURRENT_USER = "current_user";
    private static final String KEY_SHOPPING_LISTS = "shopping_lists";
    private static final String KEY_PRODUCTS = "products"; // ADDED
    private static final String TAG = "AuthManager";

    private final SharedPreferences sharedPreferences;

    public AuthManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ==================== PRODUCT MANAGEMENT METHODS ====================

    // Save product (shared across all users of the same list)
    public boolean saveProduct(Product product) {
        try {
            String productsJson = sharedPreferences.getString(KEY_PRODUCTS, "{}");
            JSONObject allProducts = new JSONObject(productsJson);

            // Get or create array for this list
            JSONArray listProducts;
            if (allProducts.has(product.getListCode())) {
                listProducts = allProducts.getJSONArray(product.getListCode());
            } else {
                listProducts = new JSONArray();
            }

            // Convert product to JSON
            JSONObject productJson = new JSONObject();
            productJson.put("id", product.getId());
            productJson.put("name", product.getName());
            productJson.put("category", product.getCategory());
            productJson.put("quantity", product.getQuantity());
            productJson.put("purchased", product.isPurchased());
            productJson.put("addedBy", product.getAddedBy());
            productJson.put("listCode", product.getListCode());
            productJson.put("notes", product.getNotes());
            productJson.put("price", product.getPrice());
            productJson.put("addedDate", product.getAddedDate());

            listProducts.put(productJson);
            allProducts.put(product.getListCode(), listProducts);

            sharedPreferences.edit().putString(KEY_PRODUCTS, allProducts.toString()).apply();
            Log.d(TAG, "Product saved: " + product.getName() + " to list: " + product.getListCode());
            return true;
        } catch (JSONException e) {
            Log.e(TAG, "Error saving product: " + e.getMessage());
            return false;
        }
    }

    // Get products for a list (shared across all users)
    public List<Product> getProductsForList(String listCode) {
        List<Product> products = new ArrayList<>();
        try {
            String productsJson = sharedPreferences.getString(KEY_PRODUCTS, "{}");
            JSONObject allProducts = new JSONObject(productsJson);

            if (allProducts.has(listCode)) {
                JSONArray listProducts = allProducts.getJSONArray(listCode);
                for (int i = 0; i < listProducts.length(); i++) {
                    JSONObject productJson = listProducts.getJSONObject(i);

                    Product product = new Product(
                            productJson.getString("name"),
                            productJson.getString("category"),
                            productJson.getInt("quantity"),
                            productJson.getString("addedBy"),
                            productJson.getString("listCode"),
                            productJson.optString("notes", ""),
                            productJson.optDouble("price", 0.0)
                    );

                    product.setId(productJson.getString("id"));
                    product.setPurchased(productJson.optBoolean("purchased", false));
                    product.setAddedDate(productJson.optLong("addedDate", System.currentTimeMillis()));

                    products.add(product);
                }
            }
            Log.d(TAG, "Retrieved " + products.size() + " products for list: " + listCode);
        } catch (JSONException e) {
            Log.e(TAG, "Error getting products: " + e.getMessage());
        }
        return products;
    }

    // Update product
    public boolean updateProduct(Product updatedProduct) {
        try {
            String productsJson = sharedPreferences.getString(KEY_PRODUCTS, "{}");
            JSONObject allProducts = new JSONObject(productsJson);

            if (!allProducts.has(updatedProduct.getListCode())) {
                return false;
            }

            JSONArray listProducts = allProducts.getJSONArray(updatedProduct.getListCode());

            // Find and update the product
            for (int i = 0; i < listProducts.length(); i++) {
                JSONObject productJson = listProducts.getJSONObject(i);
                if (productJson.getString("id").equals(updatedProduct.getId())) {
                    // Update fields
                    productJson.put("name", updatedProduct.getName());
                    productJson.put("category", updatedProduct.getCategory());
                    productJson.put("quantity", updatedProduct.getQuantity());
                    productJson.put("purchased", updatedProduct.isPurchased());
                    productJson.put("notes", updatedProduct.getNotes());
                    productJson.put("price", updatedProduct.getPrice());

                    allProducts.put(updatedProduct.getListCode(), listProducts);
                    sharedPreferences.edit().putString(KEY_PRODUCTS, allProducts.toString()).apply();
                    Log.d(TAG, "Product updated: " + updatedProduct.getName());
                    return true;
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error updating product: " + e.getMessage());
        }
        return false;
    }

    // Delete product
    public boolean deleteProduct(String productId, String listCode) {
        try {
            String productsJson = sharedPreferences.getString(KEY_PRODUCTS, "{}");
            JSONObject allProducts = new JSONObject(productsJson);

            if (!allProducts.has(listCode)) {
                return false;
            }

            JSONArray listProducts = allProducts.getJSONArray(listCode);
            JSONArray newProducts = new JSONArray();

            // Copy all except the one to delete
            for (int i = 0; i < listProducts.length(); i++) {
                JSONObject productJson = listProducts.getJSONObject(i);
                if (!productJson.getString("id").equals(productId)) {
                    newProducts.put(productJson);
                }
            }

            allProducts.put(listCode, newProducts);
            sharedPreferences.edit().putString(KEY_PRODUCTS, allProducts.toString()).apply();
            Log.d(TAG, "Product deleted: " + productId + " from list: " + listCode);
            return true;
        } catch (JSONException e) {
            Log.e(TAG, "Error deleting product: " + e.getMessage());
        }
        return false;
    }

    // Get categories for a list
    public List<String> getCategoriesForList(String listCode) {
        List<String> categories = new ArrayList<>();
        Set<String> categorySet = new HashSet<>();

        List<Product> products = getProductsForList(listCode);
        for (Product product : products) {
            String category = product.getCategory().trim();
            if (!category.isEmpty() && !"system".equals(category)) {
                categorySet.add(category);
            }
        }

        categories.addAll(categorySet);
        Log.d(TAG, "Retrieved " + categories.size() + " categories for list: " + listCode);
        return categories;
    }

    // Get list statistics
    public String getListStatistics(String listCode) {
        List<Product> products = getProductsForList(listCode);
        int totalProducts = products.size();
        int purchasedCount = 0;
        double totalCost = 0.0;

        for (Product product : products) {
            if (product.isPurchased()) {
                purchasedCount++;
            }
            totalCost += product.getTotalPrice();
        }

        return String.format("Total Products: %d\nPurchased: %d\nRemaining: %d\nTotal Cost: $%.2f",
                totalProducts, purchasedCount, totalProducts - purchasedCount, totalCost);
    }

    // ==================== EXISTING USER/LIST METHODS ====================

    public boolean registerUser(String email, String password) {
        return registerUser(email, password, User.UserRole.USER);
    }

    public boolean registerUser(String email, String password, User.UserRole role) {
        Log.d(TAG, "Attempting to register: " + email + " as " + role);

        if (userExists(email)) {
            Log.d(TAG, "User already exists: " + email);
            return false;
        }

        List<User> users = getRegisteredUsers();
        users.add(new User(email, password, role));
        saveUsers(users);

        Log.d(TAG, "Registration successful for: " + email + " as " + role);
        return true;
    }

    public boolean loginUser(String email, String password) {
        Log.d(TAG, "Attempting login for: " + email);

        List<User> users = getRegisteredUsers();

        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                sharedPreferences.edit().putString(KEY_CURRENT_USER, email).apply();
                Log.d(TAG, "Login successful for: " + email);
                return true;
            }
        }

        Log.d(TAG, "Login failed for: " + email);
        return false;
    }

    public String createShoppingList(String listName) {
        return createShoppingList(listName, "General");
    }

    public String createShoppingList(String listName, String category) {
        String currentUserEmail = getCurrentUser();
        if (currentUserEmail == null) {
            return null;
        }

        String listCode = generateListCode();
        ShoppingList shoppingList = new ShoppingList(listCode, listName, currentUserEmail, category);
        saveShoppingList(shoppingList);

        User user = getCurrentUserObject();
        if (user != null) {
            user.addCreatedList(listCode);
            user.addJoinedList(listCode);
            updateUser(user);
        }

        return listCode;
    }

    public boolean joinShoppingList(String listCode) {
        String currentUserEmail = getCurrentUser();
        if (currentUserEmail == null) {
            return false;
        }

        ShoppingList shoppingList = getShoppingList(listCode);
        if (shoppingList == null) {
            return false;
        }

        if (shoppingList.isMember(currentUserEmail)) {
            return false;
        }

        shoppingList.addMember(currentUserEmail);
        saveShoppingList(shoppingList);

        User user = getCurrentUserObject();
        if (user != null) {
            user.addJoinedList(listCode);
            updateUser(user);
        }

        return true;
    }

    public boolean leaveShoppingList(String listCode) {
        String currentUserEmail = getCurrentUser();
        if (currentUserEmail == null) {
            return false;
        }

        ShoppingList shoppingList = getShoppingList(listCode);
        if (shoppingList == null) {
            return false;
        }

        if (shoppingList.isCreator(currentUserEmail)) {
            return false;
        }

        shoppingList.removeMember(currentUserEmail);
        saveShoppingList(shoppingList);

        User user = getCurrentUserObject();
        if (user != null) {
            user.removeJoinedList(listCode);
            updateUser(user);
        }

        return true;
    }

    // In AuthManager.java - make sure this method exists and works
    public boolean removeMemberFromList(String listCode, String memberEmail) {
        String currentUserEmail = getCurrentUser();
        if (currentUserEmail == null) {
            return false;
        }

        ShoppingList shoppingList = getShoppingList(listCode);
        if (shoppingList == null) {
            return false;
        }

        // Only creator can remove members
        if (!shoppingList.isCreator(currentUserEmail)) {
            return false;
        }

        // Cannot remove the creator
        if (shoppingList.isCreator(memberEmail)) {
            return false;
        }

        // Cannot remove yourself
        if (memberEmail.equals(currentUserEmail)) {
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

        Log.d(TAG, "Member removed: " + memberEmail + " from list: " + listCode);
        return true;
    }

    private String generateListCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (getShoppingList(code) != null);
        return code;
    }

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
    // Add this method to your AuthManager.java class
    // In AuthManager.java - make sure you have this method
    public boolean updateShoppingList(String listCode, String newListName) {
        try {
            String shoppingListsJson = sharedPreferences.getString(KEY_SHOPPING_LISTS, "{}");
            JSONObject allLists = new JSONObject(shoppingListsJson);

            if (!allLists.has(listCode)) {
                return false;
            }

            JSONObject listJson = allLists.getJSONObject(listCode);
            listJson.put("listName", newListName);

            allLists.put(listCode, listJson);
            sharedPreferences.edit().putString(KEY_SHOPPING_LISTS, allLists.toString()).apply();

            Log.d(TAG, "List updated: " + listCode + " -> " + newListName);
            return true;

        } catch (JSONException e) {
            Log.e(TAG, "Error updating shopping list: " + e.getMessage());
            return false;
        }
    }

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

    private User getUserByEmail(String email) {
        List<User> users = getRegisteredUsers();
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

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

    public boolean isCurrentUserAdmin() {
        User user = getCurrentUserObject();
        return user != null && user.isAdmin();
    }

    public boolean isListCreator(String listCode) {
        String currentUserEmail = getCurrentUser();
        if (currentUserEmail == null) return false;

        ShoppingList list = getShoppingList(listCode);
        return list != null && list.isCreator(currentUserEmail);
    }

    public List<String> getListMembers(String listCode) {
        ShoppingList list = getShoppingList(listCode);
        if (list != null) {
            return list.getMemberEmails();
        }
        return new ArrayList<>();
    }

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

                if (!createdLists.isEmpty()) {
                    String[] lists = createdLists.split(",");
                    for (String listCode : lists) {
                        if (!listCode.trim().isEmpty()) {
                            user.addCreatedList(listCode.trim());
                        }
                    }
                }

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

    public void clearAllData() {
        sharedPreferences.edit().clear().apply();
        Log.d(TAG, "Cleared all data");
    }
}