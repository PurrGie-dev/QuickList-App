package com.esb.quicklist.activities;

import android.content.Intent;
import android.os.Bundle;
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
        try {
            listNameText = findViewById(R.id.listNameText);
            listCodeText = findViewById(R.id.listCodeText);
            productsLayout = findViewById(R.id.productsLayout);
            addItemButton = findViewById(R.id.addItemButton);
            adminButton = findViewById(R.id.adminButton);
            backButton = findViewById(R.id.backButton);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
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
        try {
            ShoppingList list = authManager.getShoppingList(currentListCode);
            if (list != null) {
                listNameText.setText(list.getListName());
                listCodeText.setText(String.format("Code: %s", list.getListCode()));

                boolean isCreator = authManager.isListCreator(currentListCode);
                boolean isAdmin = authManager.isCurrentUserAdmin();
                adminButton.setVisibility(isCreator || isAdmin ? View.VISIBLE : View.GONE);

                // Display members count
                TextView membersText = new TextView(this);
                membersText.setText("Members: " + list.getMemberCount());
                membersText.setPadding(0, 10, 0, 10);
                membersText.setTextSize(14);
                productsLayout.addView(membersText);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProducts() {
        productsLayout.removeAllViews();

        List<Product> products = productManager.getProductsForList(currentListCode);

        if (products.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No products in this list yet.");
            emptyText.setPadding(16, 16, 16, 16);
            emptyText.setTextSize(16);
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
        });

        productsLayout.addView(productView);
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Item to List");

        // Create layout for inputs
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Item Name (e.g., Milk)");
        layout.addView(nameInput);

        final EditText quantityInput = new EditText(this);
        quantityInput.setHint("Quantity (e.g., 2)");
        quantityInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(quantityInput);

        final EditText categoryInput = new EditText(this);
        categoryInput.setHint("Category (optional, e.g., Groceries)");
        layout.addView(categoryInput);

        builder.setView(layout);

        builder.setPositiveButton("Add Item", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String quantityStr = quantityInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();

            // Validation
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter item name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (quantityStr.isEmpty()) {
                Toast.makeText(this, "Please enter quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int quantity = Integer.parseInt(quantityStr);
                String currentUser = authManager.getCurrentUser();

                if (category.isEmpty()) {
                    category = "General";
                }

                // Create the product
                Product newProduct = new Product(
                        name,
                        category,
                        quantity,
                        currentUser,
                        currentListCode
                );

                // Save using ProductManager
                if (productManager.addProduct(newProduct)) {
                    Toast.makeText(this, "✓ Added: " + name + " (x" + quantity + ")", Toast.LENGTH_SHORT).show();
                    loadProducts(); // Refresh the list
                } else {
                    Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number for quantity", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void openListManagement() {
        Intent intent = new Intent(this, ManageListActivity.class);
        intent.putExtra("LIST_CODE", currentListCode);
        startActivity(intent);
    }
}