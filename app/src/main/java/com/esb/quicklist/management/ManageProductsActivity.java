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

import java.util.List;

public class ManageProductsActivity extends AppCompatActivity {

    private LinearLayout productsContainer;
    private Button backButton;
    private Button addProductButton;
    private TextView statisticsText;
    private AuthManager authManager;
    private ProductManager productManager;
    private String currentListCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

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
        loadProducts();
        updateStatistics();
    }

    private void initializeViews() {
        productsContainer = findViewById(R.id.productsContainer);
        backButton = findViewById(R.id.backButton);
        addProductButton = findViewById(R.id.addProductButton);
        statisticsText = findViewById(R.id.statisticsText);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        addProductButton.setOnClickListener(v -> showAddProductDialog());
    }

    private void loadProducts() {
        productsContainer.removeAllViews();

        List<Product> products = productManager.getProductsForList(currentListCode);

        if (products.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No products yet. Add some products!");
            emptyText.setPadding(16, 16, 16, 16);
            emptyText.setTextSize(16);
            productsContainer.addView(emptyText);
            return;
        }

        for (Product product : products) {
            addProductView(product);
        }
    }

    private void addProductView(Product product) {
        View productView = getLayoutInflater().inflate(R.layout.item_product, null);

        TextView productName = productView.findViewById(R.id.productName);
        TextView productDetails = productView.findViewById(R.id.productDetails);
        Button editBtn = productView.findViewById(R.id.editBtn);
        Button deleteBtn = productView.findViewById(R.id.deleteBtn);
        Button toggleBtn = productView.findViewById(R.id.toggleBtn);

        // Set product info
        productName.setText(product.getName());
        String details = "Quantity: " + product.getQuantity() +
                " | Category: " + product.getCategory() +
                " | Price: $" + String.format("%.2f", product.getPrice());
        if (!product.getNotes().isEmpty()) {
            details += "\nNotes: " + product.getNotes();
        }
        productDetails.setText(details);

        // Set purchased status
        if (product.isPurchased()) {
            productName.setAlpha(0.5f);
            productDetails.setAlpha(0.5f);
            toggleBtn.setText("Mark as Not Purchased");
        } else {
            productName.setAlpha(1.0f);
            productDetails.setAlpha(1.0f);
            toggleBtn.setText("Mark as Purchased");
        }

        // Setup button listeners
        editBtn.setOnClickListener(v -> showEditProductDialog(product));
        deleteBtn.setOnClickListener(v -> showDeleteProductDialog(product));
        toggleBtn.setOnClickListener(v -> toggleProductPurchase(product));

        productsContainer.addView(productView);
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Product");

        // Create layout for inputs
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Product Name");
        layout.addView(nameInput);

        final EditText quantityInput = new EditText(this);
        quantityInput.setHint("Quantity");
        quantityInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(quantityInput);

        final EditText categoryInput = new EditText(this);
        categoryInput.setHint("Category (e.g., Groceries)");
        layout.addView(categoryInput);

        final EditText priceInput = new EditText(this);
        priceInput.setHint("Price per item (optional)");
        priceInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(priceInput);

        final EditText notesInput = new EditText(this);
        notesInput.setHint("Notes (optional)");
        layout.addView(notesInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String quantityStr = quantityInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();
            String notes = notesInput.getText().toString().trim();

            if (name.isEmpty() || quantityStr.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int quantity = Integer.parseInt(quantityStr);
                double price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);
                String currentUser = authManager.getCurrentUser();

                Product newProduct = new Product(name, category, quantity, currentUser, currentListCode, notes, price);

                if (productManager.addProduct(newProduct)) {
                    Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
                    loadProducts();
                    updateStatistics();
                } else {
                    Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEditProductDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Product");

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

        final EditText priceInput = new EditText(this);
        priceInput.setText(String.valueOf(product.getPrice()));
        priceInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(priceInput);

        final EditText notesInput = new EditText(this);
        notesInput.setText(product.getNotes());
        layout.addView(notesInput);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String quantityStr = quantityInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();
            String notes = notesInput.getText().toString().trim();

            if (name.isEmpty() || quantityStr.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int quantity = Integer.parseInt(quantityStr);
                double price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);

                product.setName(name);
                product.setQuantity(quantity);
                product.setCategory(category);
                product.setPrice(price);
                product.setNotes(notes);

                if (productManager.updateProduct(product)) {
                    Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                    loadProducts();
                    updateStatistics();
                } else {
                    Toast.makeText(this, "Failed to update product", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteProductDialog(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (productManager.deleteProduct(product.getId())) {
                        Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
                        loadProducts();
                        updateStatistics();
                    } else {
                        Toast.makeText(this, "Failed to delete product", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toggleProductPurchase(Product product) {
        product.setPurchased(!product.isPurchased());
        if (productManager.updateProduct(product)) {
            Toast.makeText(this, "Product status updated", Toast.LENGTH_SHORT).show();
            loadProducts();
            updateStatistics();
        } else {
            Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatistics() {
        String stats = productManager.getListStatistics(currentListCode);
        statisticsText.setText(stats);
    }
}