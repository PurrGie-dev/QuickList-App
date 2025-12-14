package com.esb.quicklist.models;

import java.util.ArrayList;
import java.util.List;

public class ShoppingList {
    private String listCode;
    private String listName;
    private String creatorEmail; // The main admin who created this list
    private List<String> memberEmails; // All members including creator
    private String category; // Optional: groceries, work, etc.
    private String createdDate;

    public ShoppingList(String listCode, String listName, String creatorEmail) {
        this.listCode = listCode;
        this.listName = listName;
        this.creatorEmail = creatorEmail;
        this.memberEmails = new ArrayList<>();
        this.memberEmails.add(creatorEmail); // Creator is automatically a member
        this.category = "General";
        this.createdDate = String.valueOf(System.currentTimeMillis());
    }

    public ShoppingList(String listCode, String listName, String creatorEmail, String category) {
        this.listCode = listCode;
        this.listName = listName;
        this.creatorEmail = creatorEmail;
        this.memberEmails = new ArrayList<>();
        this.memberEmails.add(creatorEmail);
        this.category = category;
        this.createdDate = String.valueOf(System.currentTimeMillis());
    }

    // Getters and Setters
    public String getListCode() {
        return listCode;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public List<String> getMemberEmails() {
        return memberEmails;
    }

    public void addMember(String email) {
        if (!memberEmails.contains(email)) {
            memberEmails.add(email);
        }
    }

    public void removeMember(String email) {
        memberEmails.remove(email);
    }

    public boolean isMember(String email) {
        return memberEmails.contains(email);
    }

    public boolean isCreator(String email) {
        return creatorEmail.equals(email);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public int getMemberCount() {
        return memberEmails.size();
    }
}