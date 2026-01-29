package com.example.project.Course_project.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.project.Course_project.models.Budget;

import java.util.ArrayList;
import java.util.List;

public class BudgetDataBaseAccessHelper {

    //database helper instance for sqlite access
    private DatabaseHelper dbHelper;

    //constructor to initialize database helper
    public BudgetDataBaseAccessHelper(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    //insert a new monthly budget for a user
    public boolean insertBudget(Budget budget) {
        //get writable database connection
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //prepare values to insert
        ContentValues values = new ContentValues();
        values.put("category_id", budget.getCategoryId());
        values.put("user_email", budget.getUserEmail());
        values.put("month", budget.getMonth());
        values.put("limit_amount", budget.getLimitAmount());

        //insert record and check result
        long result = db.insert("budgets", null, values);
        db.close();

        //return true if insert was successful
        return result != -1;
    }

    //retrieve all budgets for a specific user
    public List<Budget> getBudgetsByUser(String userEmail) {
        //get readable database connection
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Budget> budgets = new ArrayList<>();

        //query all budgets matching user email
        Cursor cursor = db.rawQuery(
                "SELECT * FROM budgets WHERE user_email = ?",
                new String[]{userEmail}
        );

        //loop through cursor results and populate budget objects
        if (cursor.moveToFirst()) {
            do {
                Budget budget = new Budget();
                budget.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                budget.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
                budget.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));
                budget.setMonth(cursor.getString(cursor.getColumnIndexOrThrow("month")));
                budget.setLimitAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("limit_amount")));

                budgets.add(budget);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        //return list of budgets
        return budgets;
    }

    //retrieve a specific budget for a user category and month
    public Budget getBudget(String userEmail, int categoryId, String month) {
        //get readable database connection
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //query specific budget with user email, category, and month
        Cursor cursor = db.rawQuery(
                "SELECT * FROM budgets WHERE user_email = ? AND category_id = ? AND month = ?",
                new String[]{
                        userEmail,
                        String.valueOf(categoryId),
                        month
                }
        );

        //initialize budget as null
        Budget budget = null;

        //if record found, populate budget object
        if (cursor.moveToFirst()) {
            budget = new Budget();
            budget.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            budget.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
            budget.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));
            budget.setMonth(cursor.getString(cursor.getColumnIndexOrThrow("month")));
            budget.setLimitAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("limit_amount")));
        }

        cursor.close();
        db.close();

        //return budget or null if not found
        return budget;
    }

    //update an existing budget limit amount
    public boolean updateBudget(Budget budget) {
        //get writable database connection
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //prepare new values to update
        ContentValues values = new ContentValues();
        values.put("limit_amount", budget.getLimitAmount());

        //update budget where id and user email match
        int rowsAffected = db.update(
                "budgets",
                values,
                "id = ? AND user_email = ?",
                new String[]{
                        String.valueOf(budget.getId()),
                        budget.getUserEmail()
                }
        );

        db.close();

        //return true if at least one row was updated
        return rowsAffected > 0;
    }

    //delete a budget record by id and user email
    public boolean deleteBudget(int budgetId, String userEmail) {
        //get writable database connection
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //delete budget where id and user email match
        int rowsDeleted = db.delete(
                "budgets",
                "id = ? AND user_email = ?",
                new String[]{
                        String.valueOf(budgetId),
                        userEmail
                }
        );

        db.close();

        //return true if at least one row was deleted
        return rowsDeleted > 0;
    }

    //calculate total spent for a category in a given month for budget progress tracking
    public double getTotalSpentForCategory(
            String userEmail,
            int categoryId,
            String month
    ) {
        //get readable database connection
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double totalSpent = 0;

        //query sum of all expenses for category in given month
        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM transactions " +
                        "WHERE user_email = ? AND category_id = ? " +
                        "AND date LIKE ? AND type = 'EXPENSE'",
                new String[]{
                        userEmail,
                        String.valueOf(categoryId),
                        month + "%"
                }
        );

        //get total from first column if result exists
        if (cursor.moveToFirst()) {
            totalSpent = cursor.getDouble(0);
        }

        cursor.close();
        db.close();

        //return total spent amount
        return totalSpent;
    }
}
