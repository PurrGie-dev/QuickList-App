package com.esb.quicklist;

public class Product {
    private String id;
    private String name;
    private String category;
    private int quantity;
    private boolean purchased;
    private String addedBy;
    private String listCode;
    private long addedDate;
    private String notes;
    private double price;

    public Product(String name, String category, int quantity, String addedBy, String listCode) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.purchased = false;
        this.addedBy = addedBy;
        this.listCode = listCode;
        this.addedDate = System.currentTimeMillis();
        this.notes = "";
        this.price = 0.0;
    }

    public Product(String name, String category, int quantity, String addedBy, String listCode, String notes, double price) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.purchased = false;
        this.addedBy = addedBy;
        this.listCode = listCode;
        this.addedDate = System.currentTimeMillis();
        this.notes = notes;
        this.price = price;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; } // ADD THIS SETTER
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public boolean isPurchased() { return purchased; }
    public void setPurchased(boolean purchased) { this.purchased = purchased; }
    public String getAddedBy() { return addedBy; }
    public String getListCode() { return listCode; }
    public long getAddedDate() { return addedDate; }
    public void setAddedDate(long addedDate) { this.addedDate = addedDate; } // ADD THIS SETTER
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getTotalPrice() {
        return quantity * price;
    }
}