package com.example.project.Course_project.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    
    private static final String DATABASE_NAME = "finance_app.db";//database file name
    private static final int DATABASE_VERSION = 1;//database version for schema upgrades

    //constructor to initialize database helper
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //create users table to store user account information
        db.execSQL(
                "CREATE TABLE users (" +
                        "email TEXT PRIMARY KEY, " +
                        "first_name TEXT NOT NULL, " +
                        "last_name TEXT NOT NULL, " +
                        "password TEXT NOT NULL" +
                        ");"
        );

        //create categories table for income and expense types
        db.execSQL(
                "CREATE TABLE categories (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE')), " +
                        "user_email TEXT NOT NULL, " +
                        "FOREIGN KEY (user_email) REFERENCES users(email)" +
                        ");"
        );

        //create transactions table to store income and expense each transaction
        db.execSQL(
                "CREATE TABLE transactions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "amount REAL NOT NULL, " +
                        "date TEXT NOT NULL, " +
                        "description TEXT, " +
                        "type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE')), " +
                        "category_id INTEGER NOT NULL, " +
                        "user_email TEXT NOT NULL, " +
                        "FOREIGN KEY (category_id) REFERENCES categories(id), " +
                        "FOREIGN KEY (user_email) REFERENCES users(email)" +
                        ");"
        );

        //create budgets table for monthly budget tracking by category
        db.execSQL(
                "CREATE TABLE budgets (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "category_id INTEGER NOT NULL, " +
                        "user_email TEXT NOT NULL, " +
                        "month TEXT NOT NULL, " +
                        "limit_amount REAL NOT NULL, " +
                        "FOREIGN KEY (category_id) REFERENCES categories(id), " +
                        "FOREIGN KEY (user_email) REFERENCES users(email), " +
                        "UNIQUE(category_id, user_email, month)" +
                        ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //drop all existing tables to recreate schema
        db.execSQL("DROP TABLE IF EXISTS budgets");
        db.execSQL("DROP TABLE IF EXISTS transactions");
        db.execSQL("DROP TABLE IF EXISTS categories");
        db.execSQL("DROP TABLE IF EXISTS users");

        //recreate all tables with new schema
        onCreate(db);
    }
}
