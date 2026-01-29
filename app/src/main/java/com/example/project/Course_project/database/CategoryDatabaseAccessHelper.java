package com.example.project.Course_project.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.example.project.Course_project.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDatabaseAccessHelper {

   
    private DatabaseHelper dbHelper;//database helper instance for sqlite access
    private static final String TAG = "CategoryDBHelper";//tag for logging debug information
    private Context context;//application context for database operations

    //constructor to initialize database helper and context
    public CategoryDatabaseAccessHelper(Context context) {
        this.context = context.getApplicationContext();
        dbHelper = new DatabaseHelper(this.context);
    }

    //insert a new category income or expense for a user
    public boolean insertCategory(Category category) {
        //validate category object is not null
        if (category == null) {
            Log.e(TAG, "insertCategory: category is null");
            return false;
        }

        //validate user email is not null or empty
        if (category.getUserEmail() == null || category.getUserEmail().trim().isEmpty()) {
            Log.e(TAG, "insertCategory: userEmail is null or empty");
            return false;
        }

        //get writable database connection
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        try {
            //prepare values to insert
            ContentValues values = new ContentValues();
            values.put("name", category.getName());
            values.put("type", category.getType().toUpperCase());
            values.put("user_email", category.getUserEmail());

            //insert record and check result
            long result = db.insert("categories", null, values);
            success = result != -1;

            //log success or failure
            if (success) {
                Log.d(TAG, "Category inserted: " + category.getName());
            } else {
                Log.e(TAG, "Failed to insert category: " + category.getName());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inserting category: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return success;
    }

    //retrieve all categories for a user filtered by type (income or expense)
    public List<Category> getCategoriesByType(String userEmail, String type) {
        //initialize empty list for categories
        List<Category> categories = new ArrayList<>();

        //validate user email is not null
        if (userEmail == null) {
            Log.e(TAG, "getCategoriesByType: userEmail is NULL");
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            return categories;
        }

        //validate user email is not empty
        if (userEmail.trim().isEmpty()) {
            Log.e(TAG, "getCategoriesByType: userEmail is EMPTY");
            return categories;
        }

        //default type to expense if null
        if (type == null) {
            Log.e(TAG, "getCategoriesByType: type is NULL");
            type = "EXPENSE";
        }

        //convert type to uppercase
        type = type.toUpperCase();

        //validate type is either income or expense
        if (!type.equals("INCOME") && !type.equals("EXPENSE")) {
            Log.w(TAG, "getCategoriesByType: invalid type '" + type + "', using EXPENSE");
            type = "EXPENSE";
        }

        //get readable database connection
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            Log.d(TAG, "Querying categories for user: '" + userEmail + "', type: '" + type + "'");

            //prepare query parameters
            String[] selectionArgs = {
                    userEmail.trim(),
                    type.trim()
            };

            //query categories matching user email and type
            cursor = db.rawQuery(
                    "SELECT * FROM categories WHERE user_email = ? AND type = ?",
                    selectionArgs
            );

            //loop through cursor results and populate category objects
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        Category category = new Category();
                        category.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                        category.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                        category.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                        category.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));

                        categories.add(category);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing category row: " + e.getMessage());
                    }
                } while (cursor.moveToNext());

                Log.d(TAG, "Found " + categories.size() + " categories");
            } else {
                Log.d(TAG, "No categories found for user: " + userEmail);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getCategoriesByType: " + e.getMessage(), e);
            Toast.makeText(context, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        //return list of categories
        return categories;
    }

    //update an existing category name and type
    public boolean updateCategory(Category category) {
        //validate category object is not null
        if (category == null) {
            Log.e(TAG, "updateCategory: category is null");
            return false;
        }

        //validate user email is not null or empty
        if (category.getUserEmail() == null || category.getUserEmail().trim().isEmpty()) {
            Log.e(TAG, "updateCategory: userEmail is null or empty");
            return false;
        }

        //get writable database connection
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        try {
            //prepare new values to update
            ContentValues values = new ContentValues();
            values.put("name", category.getName());
            values.put("type", category.getType().toUpperCase());

            //update category where id and user email match
            int rowsAffected = db.update(
                    "categories",
                    values,
                    "id = ? AND user_email = ?",
                    new String[]{
                            String.valueOf(category.getId()),
                            category.getUserEmail()
                    }
            );

            //check if update was successful
            success = rowsAffected > 0;

            //log success or failure
            if (success) {
                Log.d(TAG, "Category updated: " + category.getName());
            } else {
                Log.w(TAG, "No category found to update: " + category.getName());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating category: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return success;
    }

    //delete a category record by id and user email
    public boolean deleteCategory(int categoryId, String userEmail) {
        //validate user email is not null or empty
        if (userEmail == null || userEmail.trim().isEmpty()) {
            Log.e(TAG, "deleteCategory: userEmail is null or empty");
            return false;
        }

        //get writable database connection
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        try {
            //delete category where id and user email match
            int rowsDeleted = db.delete(
                    "categories",
                    "id = ? AND user_email = ?",
                    new String[]{
                            String.valueOf(categoryId),
                            userEmail.trim()
                    }
            );

            //check if delete was successful
            success = rowsDeleted > 0;

            //log success or failure
            if (success) {
                Log.d(TAG, "Category deleted: id=" + categoryId);
            } else {
                Log.w(TAG, "No category found to delete: id=" + categoryId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting category: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return success;
    }

    //retrieve all categories for a user ordered by type and name
    public List<Category> getAllCategories(String userEmail) {
        //initialize empty list for categories
        List<Category> categories = new ArrayList<>();

        //validate user email is not null or empty
        if (userEmail == null || userEmail.trim().isEmpty()) {
            return categories;
        }

        //get readable database connection
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            //query all categories for user ordered by type and name
            cursor = db.rawQuery(
                    "SELECT * FROM categories WHERE user_email = ? ORDER BY type, name",
                    new String[]{userEmail.trim()}
            );

            //loop through cursor results and populate category objects
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Category category = new Category();
                    category.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    category.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    category.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                    category.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));

                    categories.add(category);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all categories: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        //return list of all categories
        return categories;
    }
}