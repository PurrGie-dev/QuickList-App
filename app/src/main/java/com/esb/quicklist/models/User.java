package com.esb.quicklist.models;

public class User {
    private String email;
    private String password;
    private UserRole role;
    private String createdLists; // Comma-separated list of shopping lists created by this user (as admin)
    private String joinedLists; // Comma-separated list of shopping lists this user has joined

    public enum UserRole {
        USER,
        ADMIN
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.role = UserRole.USER;
        this.createdLists = "";
        this.joinedLists = "";
    }

    public User(String email, String password, UserRole role) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdLists = "";
        this.joinedLists = "";
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getCreatedLists() {
        return createdLists;
    }

    public void addCreatedList(String listCode) {
        if (createdLists.isEmpty()) {
            createdLists = listCode;
        } else {
            createdLists += "," + listCode;
        }
    }

    public String getJoinedLists() {
        return joinedLists;
    }

    public void addJoinedList(String listCode) {
        if (joinedLists.isEmpty()) {
            joinedLists = listCode;
        } else {
            joinedLists += "," + listCode;
        }
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean hasCreatedList(String listCode) {
        return createdLists.contains(listCode);
    }

    public boolean hasJoinedList(String listCode) {
        return joinedLists.contains(listCode);
    }

    public boolean isListCreator(String listCode) {
        return hasCreatedList(listCode);
    }

    public void removeJoinedList(String listCode) {
        if (joinedLists.contains(listCode)) {
            String[] lists = joinedLists.split(",");
            StringBuilder newJoinedLists = new StringBuilder();
            for (String list : lists) {
                if (!list.trim().equals(listCode)) {
                    if (newJoinedLists.length() > 0) {
                        newJoinedLists.append(",");
                    }
                    newJoinedLists.append(list.trim());
                }
            }
            joinedLists = newJoinedLists.toString();
        }
    }
}