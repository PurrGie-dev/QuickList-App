package com.esb.quicklist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.esb.quicklist.utilities.AuthManager;
import com.esb.quicklist.utilities.ProductManager;
import com.esb.quicklist.management.ManageListActivity;
import com.esb.quicklist.R;
import com.esb.quicklist.models.Product;
import com.esb.quicklist.models.ShoppingList;

import java.util.List;

public class ShoppingListDetailActivity extends AppCompatActivity {

    private TextView listNameText;
    private TextView listCodeText;
    private LinearLayout productsLayout;
    private Button addItemButton;
    private Button adminButton;
    private Button backButton;
    private AuthManager authManager;
    private ProductManager productManager;
    private String currentListCode;
    private static final String TAG = "ShoppingListDetail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_detail);

        authManager = new AuthManager(this);
        productManager = new ProductManager(this);
        currentListCode = getIntent().getStringExtra("LIST_CODE");

        if (currentListCode == null) {
            Toast.makeText(this, "Error: No list selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupListeners();
        loadListData();
        loadProducts();
    }

    private void initializeViews() {
        listNameText = findViewById(R.id.listNameText);
        listCodeText = findViewById(R.id.listCodeText);
        productsLayout = findViewById(R.id.productsLayout);
        addItemButton = findViewById(R.id.addItemButton);
        adminButton = findViewById(R.id.adminButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        addItemButton.setOnClickListener(v -> showAddItemDialog());

        adminButton.setOnClickListener(v -> {
            if (authManager.isListCreator(currentListCode) || authManager.isCurrentUserAdmin()) {
                openListManagement();
            } else {
                Toast.makeText(this, "Admin privileges required", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

    private void loadListData() {
        ShoppingList list = authManager.getShoppingList(currentListCode);
        if (list != null) {
            listNameText.setText(list.getListName());
            listCodeText.setText(String.format("Code: %s", list.getListCode()));

            boolean isCreator = authManager.isListCreator(currentListCode);
            boolean isAdmin = authManager.isCurrentUserAdmin();
            adminButton.setVisibility(isCreator || isAdmin ? View.VISIBLE : View.GONE);
        }
    }

    private void loadProducts() {
        productsLayout.removeAllViews();

        List<Product> products = productManager.getProductsForList(currentListCode);

        if (products.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No items yet. Tap 'Add Item' to start!");
            emptyText.setPadding(16, 16, 16, 16);
            emptyText.setTextSize(16);
            emptyText.setGravity(View.TEXT_ALIGNMENT_CENTER);
            productsLayout.addView(emptyText);
            return;
        }

        for (Product product : products) {
            addProductItemView(product);
        }
    }

    private void addProductItemView(Product product) {
        View productView = LayoutInflater.from(this).inflate(R.layout.item_product_checkable, null);

        CheckBox checkBox = productView.findViewById(R.id.checkBox);
        TextView productNameText = productView.findViewById(R.id.productNameText);
        TextView quantityText = productView.findViewById(R.id.quantityText);
        TextView categoryText = productView.findViewById(R.id.categoryText);
        TextView addedByText = productView.findViewById(R.id.addedByText);

        productNameText.setText(product.getName());
        quantityText.setText("Qty: " + product.getQuantity());
        categoryText.setText(product.getCategory());
        addedByText.setText("Added by: " + product.getAddedBy());

        checkBox.setChecked(product.isPurchased());
        if (product.isPurchased()) {
            productNameText.setAlpha(0.5f);
            quantityText.setAlpha(0.5f);
            categoryText.setAlpha(0.5f);
            addedByText.setAlpha(0.5f);
        }

        // Make the entire item clickable for editing
        productView.setOnClickListener(v -> showEditItemDialog(product));

        // Add long press listener for delete
        productView.setOnLongClickListener(v -> {
            showDeleteItemDialog(product);
            return true; // Return true to indicate the click was handled
        });

        checkBox.setOnClickListener(v -> {
            // Don't trigger edit when checking/unchecking
            v.setClickable(false);
        });

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            product.setPurchased(isChecked);
            productManager.updateProduct(product);
            if (isChecked) {
                productNameText.setAlpha(0.5f);
                quantityText.setAlpha(0.5f);
                categoryText.setAlpha(0.5f);
                addedByText.setAlpha(0.5f);
            } else {
                productNameText.setAlpha(1.0f);
                quantityText.setAlpha(1.0f);
                categoryText.setAlpha(1.0f);
                addedByText.setAlpha(1.0f);
            }
            buttonView.setClickable(true);
        });

        productsLayout.addView(productView);
    }

    // Delete confirmation dialog for long press
    private void showDeleteItemDialog(Product product) {
        Log.d(TAG, "Showing delete dialog for product ID: " + product.getId());

        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Delete \"" + product.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteProductNow(product);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ACTUAL DELETE METHOD - This does the deletion
    private void deleteProductNow(Product product) {
        Log.d(TAG, "Attempting to delete product: " + product.getName() + " ID: " + product.getId());

        if (product.getId() == null || product.getId().isEmpty()) {
            Log.e(TAG, "Product ID is null or empty!");
            Toast.makeText(this, "Error: Invalid product ID", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean deleted = productManager.deleteProduct(product.getId());
        Log.d(TAG, "Delete result: " + deleted);

        if (deleted) {
            Toast.makeText(this, "✓ Deleted: " + product.getName(), Toast.LENGTH_SHORT).show();
            loadProducts(); // Refresh the list
        } else {
            Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Item");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Item name");
        layout.addView(nameInput);

        final EditText quantityInput = new EditText(this);
        quantityInput.setHint("Quantity");
        quantityInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        quantityInput.setText("1");
        layout.addView(quantityInput);

        final EditText categoryInput = new EditText(this);
        categoryInput.setHint("Category (optional)");
        layout.addView(categoryInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String quantityStr = quantityInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter item name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (quantityStr.isEmpty()) {
                quantityStr = "1";
            }

            try {
                int quantity = Integer.parseInt(quantityStr);
                String currentUser = authManager.getCurrentUser();

                if (category.isEmpty()) {
                    category = "General";
                }

                // Create product with proper ID generation
                Product newProduct = new Product(name, category, quantity, currentUser, currentListCode);

                // Log the ID before saving
                Log.d(TAG, "Adding new product - ID: " + newProduct.getId() + ", Name: " + name);

                if (productManager.addProduct(newProduct)) {
                    Toast.makeText(this, "✓ Added: " + name, Toast.LENGTH_SHORT).show();
                    loadProducts();
                } else {
                    Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEditItemDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Item: " + product.getName());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText nameInput = new EditText(this);
        nameInput.setText(product.getName());
        layout.addView(nameInput);

        final EditText quantityInput = new EditText(this);
        quantityInput.setText(String.valueOf(product.getQuantity()));
        quantityInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(quantityInput);

        final EditText categoryInput = new EditText(this);
        categoryInput.setText(product.getCategory());
        layout.addView(categoryInput);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String quantityStr = quantityInput.getText().toString().trim();

            if (name.isEmpty() || quantityStr.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int quantity = Integer.parseInt(quantityStr);
                product.setName(name);
                product.setQuantity(quantity);
                product.setCategory(categoryInput.getText().toString().trim());

                if (productManager.updateProduct(product)) {
                    Toast.makeText(this, "✓ Updated: " + name, Toast.LENGTH_SHORT).show();
                    loadProducts();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNeutralButton("Cancel", null);
        builder.setNegativeButton("Delete", (dialog, which) -> {
            // Delete immediately when "Delete" is clicked
            deleteProductNow(product);
        });

        builder.show();
    }

    private void openListManagement() {
        Intent intent = new Intent(this, ManageListActivity.class);
        intent.putExtra("LIST_CODE", currentListCode);
        startActivity(intent);
    }
}