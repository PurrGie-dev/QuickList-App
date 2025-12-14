package com.esb.quicklist;

public class Category {
    private String id;
    private String name;
    private String listCode;
    private String createdBy;

    public Category(String name, String listCode, String createdBy) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.name = name;
        this.listCode = listCode;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getListCode() { return listCode; }
    public String getCreatedBy() { return createdBy; }
}