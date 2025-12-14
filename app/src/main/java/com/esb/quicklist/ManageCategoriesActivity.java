package com.esb.quicklist;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManageCategoriesActivity extends AppCompatActivity {

    private LinearLayout categoriesContainer;
    private Button backButton;
    private Button addCategoryButton;
    private ProductManager productManager;
    private String currentListCode;
    private List<String> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

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
        categories = productManager.getCategoriesForList(currentListCode);

        if (categories.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText(getString(R.string.no_categories_message));
            emptyText.setPadding(16, 16, 16, 16);
            emptyText.setTextSize(16);
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
        productCountTextView.setText(getString(R.string.products_count, count));

        // Setup button listeners
        String finalCategory = category;
        editBtn.setOnClickListener(v -> showEditCategoryDialog(finalCategory));
        deleteBtn.setOnClickListener(v -> showDeleteCategoryDialog(finalCategory));

        categoriesContainer.addView(categoryView);
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.add_category));

        final EditText input = new EditText(this);
        input.setHint(getString(R.string.category_name));
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.add), (dialog, which) -> {
            String categoryName = input.getText().toString().trim();
            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (categories.contains(categoryName)) {
                Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add a product with this category to save it
            Product dummyProduct = new Product(
                    "Category Placeholder",
                    categoryName,
                    1,
                    "system",
                    currentListCode,
                    "Category placeholder",
                    0.0
            );

            if (productManager.addProduct(dummyProduct)) {
                Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
                // Now delete the dummy product
                productManager.deleteProduct(dummyProduct.getId());
                loadCategories();
            } else {
                Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    private void showEditCategoryDialog(String oldCategory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.edit_category));

        final EditText input = new EditText(this);
        input.setText(oldCategory);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.save), (dialog, which) -> {
            String newCategory = input.getText().toString().trim();
            if (newCategory.isEmpty()) {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newCategory.equals(oldCategory)) {
                return; // No change
            }

            if (categories.contains(newCategory)) {
                Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update all products with this category
            List<Product> products = productManager.getProductsForList(currentListCode);
            boolean updated = false;

            for (Product product : products) {
                if (product.getCategory().equals(oldCategory)) {
                    product.setCategory(newCategory);
                    if (productManager.updateProduct(product)) {
                        updated = true;
                    }
                }
            }

            if (updated) {
                Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show();
                loadCategories();
            } else {
                Toast.makeText(this, "No products found in this category", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    private void showDeleteCategoryDialog(String category) {
        // Check if category has products
        List<Product> products = productManager.getProductsForList(currentListCode);
        int productCount = (int) products.stream().filter(product -> product.getCategory().equals(category)).count();

        String message;
        if (productCount > 0) {
            message = String.format(Locale.getDefault(),
                    "This category has %d product(s).\n" +
                            "Deleting it will remove the category from all these products.\n" +
                            "Continue?", productCount);
        } else {
            message = "Delete this category?";
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_category))
                .setMessage(message)
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    // Update all products with this category to "Other"
                    boolean updated = false;
                    for (Product product : products) {
                        if (product.getCategory().equals(category)) {
                            product.setCategory("Other");
                            if (productManager.updateProduct(product)) {
                                updated = true;
                            }
                        }
                    }

                    if (updated || productCount == 0) {
                        Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show();
                        loadCategories();
                    } else {
                        Toast.makeText(this, "Failed to delete category", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}