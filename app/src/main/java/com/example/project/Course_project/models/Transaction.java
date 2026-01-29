package com.example.project.Course_project.models;

public class Transaction {

    private int id;
    private double amount;
    private String date;
    private String description;
    private String type;
    private int categoryId;
    private String userEmail;

    public Transaction(int id, double amount, String date, String description,
                       String type, int categoryId, String userEmail) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.type = type;
        this.categoryId = categoryId;
        this.userEmail = userEmail;
    }

    public Transaction(double amount, String date, String description,
                       String type, int categoryId, String userEmail) {
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.type = type;
        this.categoryId = categoryId;
        this.userEmail = userEmail;
    }

    public Transaction() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    
    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
