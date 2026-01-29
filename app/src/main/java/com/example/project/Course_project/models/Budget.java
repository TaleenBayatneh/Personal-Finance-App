package com.example.project.Course_project.models;

public class Budget {

    private int id;
    private int categoryId;
    private String userEmail;
    private String month;       
    private double limitAmount;

    public Budget(int id, int categoryId, String userEmail,
                  String month, double limitAmount) {
        this.id = id;
        this.categoryId = categoryId;
        this.userEmail = userEmail;
        this.month = month;
        this.limitAmount = limitAmount;
    }

    public Budget(int categoryId, String userEmail,
                  String month, double limitAmount) {
        this.categoryId = categoryId;
        this.userEmail = userEmail;
        this.month = month;
        this.limitAmount = limitAmount;
    }

    public Budget() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public double getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }
}
