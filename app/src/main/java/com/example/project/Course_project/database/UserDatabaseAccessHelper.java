package com.example.project.Course_project.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.project.Course_project.models.User;

public class UserDatabaseAccessHelper {

    
    private DatabaseHelper dbHelper;//database helper instance for sqlite access

    //constructor to initialize database helper
    public UserDatabaseAccessHelper(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    //insert a new user account into the database
    public boolean insertUser(User user) {
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();//get writable database connection

        //prepare user data values to insert
        ContentValues values = new ContentValues();
        values.put("email", user.getEmail());
        values.put("first_name", user.getFirstName());
        values.put("last_name", user.getLastName());
        values.put("password", user.getPassword());

        //insert record and check result
        long result = db.insert("users", null, values);
        db.close();

        //return true if insert was successful
        return result != -1;
    }

    //verify user login credentials by email and password
    public boolean checkLogin(String email, String password) {
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();//get readable database connection

        //query to check if user exists with matching email and password
        Cursor cursor = db.rawQuery(
                "SELECT email FROM users WHERE email = ? AND password = ?",
                new String[]{email, password}
        );

        //check if user record was found
        boolean exists = cursor.moveToFirst();

        cursor.close();
        db.close();

        //return true if credentials match
        return exists;
    }

    //retrieve user profile data by email from database
    public User getUserByEmail(String email) {
        //get readable database connection
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //query user record by email
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email = ?",
                new String[]{email}
        );

        //initialize user as null
        User user = null;

        //if user found, populate user object with data
        if (cursor.moveToFirst()) {
            user = new User();
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            user.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
            user.setLastName(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
        }

        cursor.close();
        db.close();

        //return user or null if not found
        return user;
    }

    //update user profile information including name and password
    public boolean updateUser(User user) {
       
        SQLiteDatabase db = dbHelper.getWritableDatabase();//get writable database connection

        //prepare new values to update
        ContentValues values = new ContentValues();
        values.put("first_name", user.getFirstName());
        values.put("last_name", user.getLastName());
        values.put("password", user.getPassword());

        //update user record where email matches
        int rowsAffected = db.update(
                "users",
                values,
                "email = ?",
                new String[]{user.getEmail()}
        );

        db.close();

        //return true if at least one row was updated
        return rowsAffected > 0;
    }
}