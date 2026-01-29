package com.example.project.Course_project.models;

public class Category {

    private int id;
    private String name;
    private String type;
    private String userEmail;

    public Category(int id, String name, String type, String userEmail) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.userEmail = userEmail;
    }

    public Category(String name, String type, String userEmail) {
        this.name = name;
        this.type = type;
        this.userEmail = userEmail;
    }

    public Category() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}

