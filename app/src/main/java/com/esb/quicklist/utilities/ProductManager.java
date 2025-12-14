package com.esb.quicklist.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.esb.quicklist.models.Product;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ProductManager {
    private static final String PREF_NAME = "ProductPrefs";
    private static final String KEY_PRODUCTS = "all_products";
    private static final String TAG = "ProductManager";

    private final SharedPreferences sharedPreferences;
    private final AuthManager authManager;

    public ProductManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        authManager = new AuthManager(context);
    }

    // Save product to SharedPreferences
    public boolean addProduct(Product product) {
        try {
            // Get all products
            JSONObject allProducts = getAllProductsJson();

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

            // Add to array
            listProducts.put(productJson);

            // Save back
            allProducts.put(product.getListCode(), listProducts);
            saveAllProducts(allProducts);

            Log.d(TAG, "✓ Product added: " + product.getName() +
                    " ID: " + product.getId() +
                    " to list: " + product.getListCode());
            return true;
        } catch (JSONException e) {
            Log.e(TAG, "Error adding product: " + e.getMessage());
            return false;
        }
    }

    // Get products for a specific list
    public List<Product> getProductsForList(String listCode) {
        List<Product> products = new ArrayList<>();
        try {
            JSONObject allProducts = getAllProductsJson();

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

    // Update an existing product
    public boolean updateProduct(Product updatedProduct) {
        try {
            JSONObject allProducts = getAllProductsJson();

            if (!allProducts.has(updatedProduct.getListCode())) {
                Log.e(TAG, "List not found: " + updatedProduct.getListCode());
                return false;
            }

            JSONArray listProducts = allProducts.getJSONArray(updatedProduct.getListCode());

            // Find and update the product
            for (int i = 0; i < listProducts.length(); i++) {
                JSONObject productJson = listProducts.getJSONObject(i);
                if (productJson.getString("id").equals(updatedProduct.getId())) {
                    // Update all fields
                    productJson.put("name", updatedProduct.getName());
                    productJson.put("category", updatedProduct.getCategory());
                    productJson.put("quantity", updatedProduct.getQuantity());
                    productJson.put("purchased", updatedProduct.isPurchased());
                    productJson.put("notes", updatedProduct.getNotes());
                    productJson.put("price", updatedProduct.getPrice());

                    // Save back
                    allProducts.put(updatedProduct.getListCode(), listProducts);
                    saveAllProducts(allProducts);

                    Log.d(TAG, "✓ Product updated: " + updatedProduct.getName() +
                            " ID: " + updatedProduct.getId());
                    return true;
                }
            }

            Log.e(TAG, "✗ Product not found for update: " + updatedProduct.getId());
        } catch (JSONException e) {
            Log.e(TAG, "Error updating product: " + e.getMessage());
        }
        return false;
    }

    // Delete a product - FIXED VERSION
    public boolean deleteProduct(String productId) {
        try {
            Log.d(TAG, "DELETE: Attempting to delete product ID: " + productId);

            if (productId == null || productId.isEmpty()) {
                Log.e(TAG, "DELETE: Product ID is null or empty!");
                return false;
            }

            JSONObject allProducts = getAllProductsJson();
            boolean found = false;

            // Get all keys (list codes)
            Iterator<String> keys = allProducts.keys();

            while (keys.hasNext()) {
                String listCode = keys.next();
                JSONArray listProducts = allProducts.getJSONArray(listCode);
                JSONArray newProducts = new JSONArray();

                // Copy all except the one to delete
                for (int i = 0; i < listProducts.length(); i++) {
                    JSONObject productJson = listProducts.getJSONObject(i);
                    String jsonId = productJson.getString("id");

                    if (jsonId.equals(productId)) {
                        found = true;
                        Log.d(TAG, "✓ DELETE: Found and removing product: " +
                                productJson.getString("name") + " ID: " + jsonId);
                    } else {
                        newProducts.put(productJson);
                    }
                }

                // Update the list
                if (newProducts.length() > 0) {
                    allProducts.put(listCode, newProducts);
                } else {
                    allProducts.remove(listCode);
                    Log.d(TAG, "DELETE: Removed empty list: " + listCode);
                }
            }

            if (found) {
                saveAllProducts(allProducts);
                Log.d(TAG, "✓ DELETE: Successfully deleted product ID: " + productId);
                return true;
            } else {
                Log.e(TAG, "✗ DELETE: Product not found with ID: " + productId);
                // Debug: Log all existing products
                logAllProducts();
                return false;
            }
        } catch (JSONException e) {
            Log.e(TAG, "DELETE: Error deleting product: " + e.getMessage());
            return false;
        }
    }

    // Helper to log all products for debugging
    private void logAllProducts() {
        try {
            JSONObject allProducts = getAllProductsJson();
            Log.d(TAG, "DEBUG: All products in storage:");

            Iterator<String> keys = allProducts.keys();
            while (keys.hasNext()) {
                String listCode = keys.next();
                JSONArray listProducts = allProducts.getJSONArray(listCode);
                Log.d(TAG, "  List: " + listCode + " has " + listProducts.length() + " products");

                for (int i = 0; i < listProducts.length(); i++) {
                    JSONObject product = listProducts.getJSONObject(i);
                    Log.d(TAG, "    - ID: " + product.getString("id") +
                            ", Name: " + product.getString("name"));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "DEBUG: Error logging products: " + e.getMessage());
        }
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

    // Get statistics for a list
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

    // Helper method to get all products JSON
    private JSONObject getAllProductsJson() {
        try {
            String productsJson = sharedPreferences.getString(KEY_PRODUCTS, "{}");
            return new JSONObject(productsJson);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing products JSON: " + e.getMessage());
            return new JSONObject();
        }
    }

    // Helper method to save all products
    private void saveAllProducts(JSONObject allProducts) {
        sharedPreferences.edit().putString(KEY_PRODUCTS, allProducts.toString()).apply();
        Log.d(TAG, "SAVED: " + allProducts.toString());
    }

    // Get all list codes that have products
    private Set<String> getAllListCodes() {
        Set<String> listCodes = new HashSet<>();
        try {
            JSONObject allProducts = getAllProductsJson();
            Iterator<String> keys = allProducts.keys();
            while (keys.hasNext()) {
                listCodes.add(keys.next());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting list codes: " + e.getMessage());
        }
        return listCodes;
    }

    // Clear all products (for testing)
    public void clearAllProducts() {
        sharedPreferences.edit().remove(KEY_PRODUCTS).apply();
        Log.d(TAG, "All products cleared");
    }

    public void clearProductsForList(String currentListCode) {
        try {
            JSONObject allProducts = getAllProductsJson();
            allProducts.remove(currentListCode);
            saveAllProducts(allProducts);
            Log.d(TAG, "Cleared products for list: " + currentListCode);
        } catch (Exception e) {
            Log.e(TAG, "Error clearing products: " + e.getMessage());
        }
    }
}