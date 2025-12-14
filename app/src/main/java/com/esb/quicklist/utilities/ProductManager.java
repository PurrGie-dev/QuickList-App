package com.esb.quicklist.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.esb.quicklist.models.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // ADD THIS IMPORT

public class ProductManager {
    private static final String TAG = "ProductManager";
    private static final String PREF_NAME = "ProductPrefs";
    private static final String KEY_PRODUCTS = "products";

    private final SharedPreferences sharedPreferences;

    public ProductManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Add a new product
    public boolean addProduct(Product product) {
        try {
            List<Product> allProducts = getAllProducts();
            allProducts.add(product);
            saveProducts(allProducts);
            Log.d(TAG, "Product added: " + product.getName());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error adding product: " + e.getMessage());
            return false;
        }
    }

    // Get all products for a specific list
    public List<Product> getProductsForList(String listCode) {
        List<Product> listProducts = new ArrayList<>();
        List<Product> allProducts = getAllProducts();

        for (Product product : allProducts) {
            if (product.getListCode().equals(listCode)) {
                listProducts.add(product);
            }
        }
        return listProducts;
    }

    // Update a product
    public boolean updateProduct(Product updatedProduct) {
        try {
            List<Product> allProducts = getAllProducts();
            for (int i = 0; i < allProducts.size(); i++) {
                if (allProducts.get(i).getId().equals(updatedProduct.getId())) {
                    allProducts.set(i, updatedProduct);
                    saveProducts(allProducts);
                    Log.d(TAG, "Product updated: " + updatedProduct.getName());
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error updating product: " + e.getMessage());
            return false;
        }
    }

    // Delete a product
    public boolean deleteProduct(String productId) {
        try {
            List<Product> allProducts = getAllProducts();
            for (int i = 0; i < allProducts.size(); i++) {
                if (allProducts.get(i).getId().equals(productId)) {
                    allProducts.remove(i);
                    saveProducts(allProducts);
                    Log.d(TAG, "Product deleted: " + productId);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting product: " + e.getMessage());
            return false;
        }
    }

    // Mark product as purchased/unpurchased
    public boolean togglePurchaseStatus(String productId, boolean purchased) {
        try {
            List<Product> allProducts = getAllProducts();
            for (Product product : allProducts) {
                if (product.getId().equals(productId)) {
                    product.setPurchased(purchased);
                    saveProducts(allProducts);
                    Log.d(TAG, "Product purchase status updated: " + productId + " = " + purchased);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error toggling purchase status: " + e.getMessage());
            return false;
        }
    }

    // Get all categories for a list
    public List<String> getCategoriesForList(String listCode) {
        List<String> categories = new ArrayList<>();
        List<Product> products = getProductsForList(listCode);

        for (Product product : products) {
            String category = product.getCategory();
            if (!category.isEmpty() && !categories.contains(category)) {
                categories.add(category);
            }
        }

        // Add default categories if none exist
        if (categories.isEmpty()) {
            categories.add("Groceries");
            categories.add("Household");
            categories.add("Personal Care");
            categories.add("Other");
        }

        return categories;
    }

    // Get statistics for a list
    public String getListStatistics(String listCode) {
        List<Product> products = getProductsForList(listCode);
        int totalItems = 0;
        int purchasedItems = 0;
        double totalCost = 0.0;

        for (Product product : products) {
            totalItems += product.getQuantity();
            if (product.isPurchased()) {
                purchasedItems += product.getQuantity();
            }
            totalCost += product.getTotalPrice();
        }

        return String.format(Locale.getDefault(),
                "Total Items: %d\nPurchased: %d\nRemaining: %d\nTotal Cost: $%.2f",
                totalItems, purchasedItems, (totalItems - purchasedItems), totalCost);
    }

    // PRIVATE HELPER METHODS
    private List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String productsJson = sharedPreferences.getString(KEY_PRODUCTS, "[]");

        try {
            JSONArray jsonArray = new JSONArray(productsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                Product product = new Product(
                        jsonObject.getString("name"),
                        jsonObject.getString("category"),
                        jsonObject.getInt("quantity"),
                        jsonObject.getString("addedBy"),
                        jsonObject.getString("listCode"),
                        jsonObject.optString("notes", ""),
                        jsonObject.optDouble("price", 0.0)
                );

                // Set additional fields
                product.setId(jsonObject.getString("id")); // USE SETTER
                product.setPurchased(jsonObject.getBoolean("purchased"));
                product.setAddedDate(jsonObject.getLong("addedDate")); // USE SETTER

                products.add(product);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing products: " + e.getMessage());
        }

        return products;
    }

    private void saveProducts(List<Product> products) {
        JSONArray jsonArray = new JSONArray();

        try {
            for (Product product : products) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", product.getId());
                jsonObject.put("name", product.getName());
                jsonObject.put("category", product.getCategory());
                jsonObject.put("quantity", product.getQuantity());
                jsonObject.put("purchased", product.isPurchased());
                jsonObject.put("addedBy", product.getAddedBy());
                jsonObject.put("listCode", product.getListCode());
                jsonObject.put("addedDate", product.getAddedDate());
                jsonObject.put("notes", product.getNotes());
                jsonObject.put("price", product.getPrice());

                jsonArray.put(jsonObject);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error saving products: " + e.getMessage());
        }

        sharedPreferences.edit().putString(KEY_PRODUCTS, jsonArray.toString()).apply();
    }
}