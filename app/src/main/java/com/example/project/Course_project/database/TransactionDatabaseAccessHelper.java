package com.example.project.Course_project.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.project.Course_project.models.Transaction;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;


public class TransactionDatabaseAccessHelper {

    
    private DatabaseHelper dbHelper;//database helper instance for sqlite access

    //constructor to initialize database helper
    public TransactionDatabaseAccessHelper(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    //insert a new income or expense transaction record
    public boolean insertTransaction(Transaction transaction) {
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();//get writable database connection

        //prepare values to insert
        ContentValues values = new ContentValues();
        values.put("amount", transaction.getAmount());
        values.put("date", transaction.getDate()); 
        values.put("description", transaction.getDescription());
        values.put("type", transaction.getType()); 
        values.put("category_id", transaction.getCategoryId());
        values.put("user_email", transaction.getUserEmail());

        //insert record and check result
        long result = db.insert("transactions", null, values);
        db.close();

        //return true if insert was successful
        return result != -1;
    }

    //retrieve all transactions for a user sorted by date in descending order
    public List<Transaction> getAllTransactions(String userEmail) {
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();//get readable database connection
        List<Transaction> transactions = new ArrayList<>();

        //query all transactions for user ordered by date descending
        Cursor cursor = db.rawQuery(
                "SELECT * FROM transactions WHERE user_email = ? ORDER BY date DESC",
                new String[]{userEmail}
        );

        //loop through cursor results and populate transaction objects
        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
                transaction.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                transaction.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
                transaction.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));

                transactions.add(transaction);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        //return list of all transactions
        return transactions;
    }

    //retrieve transactions filtered by type (income or expense) sorted by date
    public List<Transaction> getTransactionsByType(String userEmail, String type) {
       
        SQLiteDatabase db = dbHelper.getReadableDatabase();//get readable database connection
        List<Transaction> transactions = new ArrayList<>();

        //query transactions matching user email and type ordered by date descending
        Cursor cursor = db.rawQuery(
                "SELECT * FROM transactions WHERE user_email = ? AND type = ? ORDER BY date DESC",
                new String[]{userEmail, type}
        );

        //loop through cursor results and populate transaction objects
        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
                transaction.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                transaction.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
                transaction.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));

                transactions.add(transaction);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        //return list of transactions by type
        return transactions;
    }

    //retrieve transactions by type sorted by date in descending order
    public List<Transaction> getTransactionsByTypeSortedByDate(
            String userEmail,
            String type
    ) {
        //initialize empty list for transactions
        List<Transaction> list = new ArrayList<>();
        //get readable database connection
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //query transactions matching user email and type ordered by date descending
        Cursor cursor = db.rawQuery(
                "SELECT * FROM transactions " +
                        "WHERE user_email=? AND type=? " +
                        "ORDER BY date DESC",
                new String[]{userEmail, type}
        );

        //loop through cursor results and populate transaction objects
        while (cursor.moveToNext()) {
            Transaction t = new Transaction();
            t.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            t.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
            t.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
            t.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
            t.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            t.setType(type);
            t.setUserEmail(userEmail);
            list.add(t);
        }

        cursor.close();
        //return list of transactions sorted by date
        return list;
    }

    //update an existing transaction record with new values
    public boolean updateTransaction(Transaction transaction) {
        //get writable database connection
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //prepare new values to update
        ContentValues values = new ContentValues();
        values.put("amount", transaction.getAmount());
        values.put("date", transaction.getDate());
        values.put("description", transaction.getDescription());
        values.put("category_id", transaction.getCategoryId());

        //update transaction where id and user email match
        int rowsAffected = db.update(
                "transactions",
                values,
                "id = ? AND user_email = ?",
                new String[]{
                        String.valueOf(transaction.getId()),
                        transaction.getUserEmail()
                }
        );

        db.close();

        //return true if at least one row was updated
        return rowsAffected > 0;
    }

    //retrieve total transaction amount by type for summary and reports
    public double getTotalAmountByType(String userEmail, String type) {
        //get readable database connection
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double total = 0;

        //query sum of all transactions matching user email and type
        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM transactions WHERE user_email = ? AND type = ?",
                new String[]{userEmail, type}
        );

        //get total from first column if result exists
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        db.close();

        //return total amount
        return total;
    }

    //calculate total expenses for a category in a specific month for budget tracking
    public double getTotalExpensesForCategoryAndMonth(
            String userEmail, int categoryId, String month
    ) {
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();//get readable database connection

        //query sum of expenses for category matching month pattern
        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM transactions " +
                        "WHERE user_email = ? AND category_id = ? " +
                        "AND type = 'EXPENSE' AND date LIKE ?",
                new String[]{ userEmail, String.valueOf(categoryId), month + "%" }
        );

        //get total from first column if result exists
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }

        Log.d("BUDGET_DEBUG",
                "email=" + userEmail +
                        ", categoryId=" + categoryId +
                        ", month=" + month);

        //return 0 if no expenses found
        return 0;
    }

    //delete a transaction record by id and user email
    public void deleteTransaction(int transactionId, String userEmail) {
       
        SQLiteDatabase db = dbHelper.getWritableDatabase();//get writable database connection
        //delete transaction where id and user email match
        db.delete(
                "transactions",
                "id = ? AND user_email = ?",
                new String[]{String.valueOf(transactionId), userEmail}
        );
        db.close();
    }


}
