package com.example.project.Course_project.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.project.R;
import com.example.project.Course_project.database.CategoryDatabaseAccessHelper;
import com.example.project.Course_project.database.TransactionDatabaseAccessHelper;
import com.example.project.Course_project.models.Category;
import com.example.project.Course_project.models.Transaction;
import com.example.project.Course_project.utils.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// represents the income fragment where users can add, update, and manage their income transactions
public class IncomeFragment extends Fragment {

    // ui components for entering income data
    private Spinner categorySpinner;
    private EditText amountEditText, descriptionEditText, dateEditText;
    private ListView incomeListView;

    // database helpers for managing category and transaction data
    private CategoryDatabaseAccessHelper categoryDao;
    private TransactionDatabaseAccessHelper transactionDao;

    // stores user email, categories, and current list of transactions
    private String userEmail;
    private List<Category> categories;
    private List<Transaction> transactions = new ArrayList<>();

    // keeps track of the currently selected transaction for editing
    private Transaction selectedTransaction = null;
    private Button addButton;


    // default constructor required for fragment instantiation
    public IncomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_income, container, false);

        // bind all ui elements from the layout
        categorySpinner = view.findViewById(R.id.spinner_category);
        amountEditText = view.findViewById(R.id.editText_amount);
        descriptionEditText = view.findViewById(R.id.editText_description);
        dateEditText = view.findViewById(R.id.editText_date);
        incomeListView = view.findViewById(R.id.list_income);
        addButton = view.findViewById(R.id.button_addincome);

        // initialize database helpers for accessing income and category data
        categoryDao = new CategoryDatabaseAccessHelper(requireContext());
        transactionDao = new TransactionDatabaseAccessHelper(requireContext());

        // retrieve the current logged-in user's email from session
        SessionManager prefManager = new SessionManager(requireContext());
        userEmail = prefManager.getUserEmail();

        // setup the date picker functionality and load initial data
        setupDatePicker();
        loadCategories();
        loadIncome();

        // handle long click on income item to delete it
        incomeListView.setOnItemLongClickListener((parent, view1, position, id) -> {

            // ensure the position is valid before attempting to access the transaction
            if (transactions.isEmpty() || position >= transactions.size()) {
                return true;
            }

            // get the selected transaction and show a confirmation dialog
            Transaction selected = transactions.get(position);

            // confirm deletion before removing the income
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Income")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Delete", (d, w) -> {
                        // delete from database and refresh the list
                        transactionDao.deleteTransaction(
                                selected.getId(),
                                userEmail
                        );
                        loadIncome();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });

        // handle click on income item to load it for editing
        incomeListView.setOnItemClickListener((parent, view2, position, id) -> {

            // ensure the position is valid before attempting to access the transaction
            if (transactions.isEmpty() || position >= transactions.size()) return;

            // get the selected transaction and populate the form fields
            selectedTransaction = transactions.get(position);

            // populate all fields with the selected transaction's data
            amountEditText.setText(String.valueOf(selectedTransaction.getAmount()));
            descriptionEditText.setText(selectedTransaction.getDescription());
            dateEditText.setText(selectedTransaction.getDate());

            // change button text to indicate update mode
            addButton.setText("Update Income");
        });

        // handle add/update button click events
        addButton.setOnClickListener(v -> addIncome());

        return view;
    }

    // setup a date picker that opens when the user clicks on the date field
    private void setupDatePicker() {
        dateEditText.setOnClickListener(v -> {
            // get current calendar instance to show today's date by default
            Calendar calendar = Calendar.getInstance();

            // show the date picker dialog and format the selected date
            new DatePickerDialog(
                    requireContext(),
                    (view, year, month, day) -> {
                        // format date as yyyy-mm-dd
                        String formattedDate = String.format(
                                Locale.getDefault(),
                                "%04d-%02d-%02d",
                                year,
                                month + 1,
                                day
                        );
                        dateEditText.setText(formattedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    // fetch and populate the category spinner with income categories
    private void loadCategories() {
        // ensure user is logged in before loading categories
        if (userEmail == null) {
            return;
        }
        // retrieve all income categories for the current user
        categories = categoryDao.getCategoriesByType(userEmail, "INCOME");

        // extract category names for the spinner display
        List<String> names = new ArrayList<>();
        for (Category c : categories) {
            names.add(c.getName());
        }

        // create an adapter and bind it to the category spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                names
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    // add a new income or update an existing one based on user input
    private void addIncome() {
        // validate that user is logged in and categories are available
        if (userEmail == null || categories.isEmpty()) {
            Toast.makeText(getContext(), "Missing data", Toast.LENGTH_SHORT).show();
            return;
        }

        // retrieve income details from form fields
        String amountStr = amountEditText.getText().toString();
        String date = dateEditText.getText().toString();

        // ensure all required fields are filled before processing
        if (amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(getContext(), "Fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // parse the amount and get the selected category
        double amount = Double.parseDouble(amountStr);
        Category selectedCategory = categories.get(categorySpinner.getSelectedItemPosition());

        // check if we're adding a new income or updating an existing one
        if (selectedTransaction == null) {
            // create and save a new income transaction
            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            transaction.setDate(date);
            transaction.setCategoryId(selectedCategory.getId());
            transaction.setDescription(descriptionEditText.getText().toString());
            transaction.setType("INCOME");
            transaction.setUserEmail(userEmail);

            transactionDao.insertTransaction(transaction);

        } else {
            // update the selected income with new values
            selectedTransaction.setAmount(amount);
            selectedTransaction.setDate(date);
            selectedTransaction.setCategoryId(selectedCategory.getId());
            selectedTransaction.setDescription(descriptionEditText.getText().toString());

            transactionDao.updateTransaction(selectedTransaction);
            
            // clear selection and reset button text back to "add" mode
            selectedTransaction = null;
            addButton.setText("Add Income");
        }

        // clear all form fields after successfully saving
        amountEditText.setText("");
        descriptionEditText.setText("");
        dateEditText.setText("");

        Toast.makeText(getContext(), "Income added successfully", Toast.LENGTH_SHORT).show();
        
        // refresh the income list to show the newly added or updated item
        loadIncome();
    }

    // fetch all income transactions from the database and update the list view
    private void loadIncome() {
        // ensure user is logged in before loading income
        if (userEmail == null) return;

        // clear the list and reload fresh data from the database
        transactions.clear();
        transactions.addAll(
                transactionDao.getTransactionsByType(userEmail, "INCOME")
        );

        // format transaction data for display in the list view
        List<String> displayList = new ArrayList<>();
        for (Transaction t : transactions) {
            // create a formatted string showing date, amount, and description
            displayList.add(
                    t.getDate() + " | " +
                            t.getAmount() + " | " +
                            t.getDescription()
            );
        }

        // bind the formatted list to the list view adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                displayList
        );

        incomeListView.setAdapter(adapter);
    }

}