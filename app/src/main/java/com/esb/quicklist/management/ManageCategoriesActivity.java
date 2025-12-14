package com.esb.quicklist.management;

import android.os.Bundle;
import android.view.View;
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
import com.esb.quicklist.models.Product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ManageCategoriesActivity extends AppCompatActivity {

    private LinearLayout categoriesContainer;
    private Button backButton;
    private Button addCategoryButton;
    private AuthManager authManager;
    private ProductManager productManager;
    private String currentListCode;
    private List<String> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

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
        loadCategories();
    }

    private void initializeViews() {
        categoriesContainer = findViewById(R.id.categoriesContainer);
        backButton = findViewById(R.id.backButton);
        addCategoryButton = findViewById(R.id.addCategoryButton);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        addCategoryButton.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void loadCategories() {
        categoriesContainer.removeAllViews();

        // Get unique categories from products
        List<Product> products = productManager.getProductsForList(currentListCode);
        Set<String> uniqueCategories = new HashSet<>();

        for (Product product : products) {
            String category = product.getCategory().trim();
            if (!category.isEmpty() && !category.equals("General") && !category.equals("system")) {
                uniqueCategories.add(category);
            }
        }

        categories = new ArrayList<>(uniqueCategories);

        if (categories.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No categories yet. Add some products with categories first!");
            emptyText.setPadding(16, 16, 16, 16);
            emptyText.setTextSize(16);
            emptyText.setGravity(View.TEXT_ALIGNMENT_CENTER);
            categoriesContainer.addView(emptyText);
            return;
        }

        for (String category : categories) {
            addCategoryView(category);
        }
    }

    private void addCategoryView(String category) {
        View categoryView = getLayoutInflater().inflate(R.layout.item_category, null);

        TextView categoryNameTextView = categoryView.findViewById(R.id.categoryNameTextView);
        TextView productCountTextView = categoryView.findViewById(R.id.productCountTextView);
        Button editBtn = categoryView.findViewById(R.id.editCategoryBtn);
        Button deleteBtn = categoryView.findViewById(R.id.deleteCategoryBtn);

        // Set category info
        categoryNameTextView.setText(category);

        // Count products in this category
        List<Product> products = productManager.getProductsForList(currentListCode);
        int count = 0;
        for (Product product : products) {
            if (product.getCategory().equals(category)) {
                count++;
            }
        }
        productCountTextView.setText(count + " product(s)");

        // Setup button listeners
        String finalCategory = category;
        editBtn.setOnClickListener(v -> showEditCategoryDialog(finalCategory));
        deleteBtn.setOnClickListener(v -> showDeleteCategoryDialog(finalCategory));

        categoriesContainer.addView(categoryView);
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Category");

        final EditText input = new EditText(this);
        input.setHint("Category name (e.g., Groceries, Electronics)");
        builder.setView(input);

        builder.setPositiveButton("Add Category", (dialog, which) -> {
            String categoryName = input.getText().toString().trim();

            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (categoryName.equalsIgnoreCase("system")) {
                Toast.makeText(this, "Category name 'system' is reserved", Toast.LENGTH_SHORT).show();
                return;
            }

            if (categories.contains(categoryName)) {
                Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add a dummy product with this category to create it
            String currentUser = authManager.getCurrentUser();
            Product dummyProduct = new Product(
                    "Category Placeholder",
                    categoryName,
                    1,
                    currentUser,
                    currentListCode,
                    "Auto-generated category placeholder",
                    0.0
            );

            if (productManager.addProduct(dummyProduct)) {
                // Now immediately delete the dummy product
                productManager.deleteProduct(dummyProduct.getId());

                Toast.makeText(this, "✓ Category added: " + categoryName, Toast.LENGTH_SHORT).show();
                loadCategories();
            } else {
                Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEditCategoryDialog(String oldCategory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Category: " + oldCategory);

        final EditText input = new EditText(this);
        input.setText(oldCategory);
        builder.setView(input);

        builder.setPositiveButton("Save Changes", (dialog, which) -> {
            String newCategory = input.getText().toString().trim();

            if (newCategory.isEmpty()) {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newCategory.equals(oldCategory)) {
                Toast.makeText(this, "No changes made", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newCategory.equalsIgnoreCase("system")) {
                Toast.makeText(this, "Category name 'system' is reserved", Toast.LENGTH_SHORT).show();
                return;
            }

            if (categories.contains(newCategory)) {
                Toast.makeText(this, "Category '" + newCategory + "' already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update all products with the old category to the new category
            List<Product> products = productManager.getProductsForList(currentListCode);
            boolean updated = false;
            int updatedCount = 0;

            for (Product product : products) {
                if (product.getCategory().equals(oldCategory)) {
                    product.setCategory(newCategory);
                    if (productManager.updateProduct(product)) {
                        updated = true;
                        updatedCount++;
                    }
                }
            }

            if (updated) {
                Toast.makeText(this, "✓ Updated " + updatedCount + " product(s) to category: " + newCategory, Toast.LENGTH_SHORT).show();
                loadCategories();
            } else {
                Toast.makeText(this, "No products found in this category", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteCategoryDialog(String category) {
        // Count products in this category
        List<Product> products = productManager.getProductsForList(currentListCode);
        int productCount = (int) products.stream().filter(product -> product.getCategory().equals(category)).count();

        String message;
        if (productCount > 0) {
            message = String.format(Locale.getDefault(),
                    "⚠️ This category has %d product(s).\n\n" +
                            "What would you like to do?",
                    productCount);
        } else {
            message = "Delete this category?";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Delete Category: " + category);

        if (productCount > 0) {
            // If there are products, show options
            builder.setMessage(message)
                    .setPositiveButton("Move to 'General' & Delete", (dialog, which) -> {
                        moveProductsToGeneralAndDeleteCategory(category, productCount);
                    })
                    .setNegativeButton("Cancel", null);
        } else {
            // If no products, just delete
            builder.setMessage(message)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        Toast.makeText(this, "✓ Category deleted: " + category, Toast.LENGTH_SHORT).show();
                        loadCategories();
                    })
                    .setNegativeButton("Cancel", null);
        }

        builder.show();
    }

    private void moveProductsToGeneralAndDeleteCategory(String category, int productCount) {
        List<Product> products = productManager.getProductsForList(currentListCode);
        int movedCount = 0;

        for (Product product : products) {
            if (product.getCategory().equals(category)) {
                product.setCategory("General");
                if (productManager.updateProduct(product)) {
                    movedCount++;
                }
            }
        }

        if (movedCount > 0) {
            Toast.makeText(this, "✓ Moved " + movedCount + " product(s) to 'General' and deleted category: " + category, Toast.LENGTH_SHORT).show();
            loadCategories();
        } else {
            Toast.makeText(this, "Failed to update products", Toast.LENGTH_SHORT).show();
        }
    }
}