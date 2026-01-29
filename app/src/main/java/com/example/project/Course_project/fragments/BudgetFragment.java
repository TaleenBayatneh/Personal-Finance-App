package com.example.project.Course_project.fragments;

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
import com.example.project.Course_project.adapters.BudgetWarningAdapter;
import com.example.project.Course_project.database.BudgetDataBaseAccessHelper;
import com.example.project.Course_project.database.CategoryDatabaseAccessHelper;
import com.example.project.Course_project.database.TransactionDatabaseAccessHelper;
import com.example.project.Course_project.models.Budget;
import com.example.project.Course_project.models.Category;
//import com.example.project.ui.utils.SharedPrefManager;
import com.example.project.Course_project.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;


public class BudgetFragment extends Fragment {

    // parameter constants
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // fragment parameters
    private String mParam1;
    private String mParam2;

    // ui elements for the budget screen
    private Spinner categorySpinner;
    private EditText budgetAmountEditText;
    private ListView budgetsListView;
    private ListView warningsListView;

    // database helpers to access data
    private BudgetDataBaseAccessHelper budgetDao;
    private CategoryDatabaseAccessHelper categoryDao;
    private TransactionDatabaseAccessHelper transactionDao;

    // user and category data
    private String userEmail;
    private List<Category> categories;

    public BudgetFragment() {
        //  empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BudgetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BudgetFragment newInstance(String param1, String param2) {
        BudgetFragment fragment = new BudgetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get the arguments passed to this fragment
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // create the view from the layout file
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        // find all the ui elements on the screen
        categorySpinner = view.findViewById(R.id.spinner_category);
        budgetAmountEditText = view.findViewById(R.id.et_budget_amount);
        budgetsListView = view.findViewById(R.id.list_budgets);
        warningsListView = view.findViewById(R.id.list_budget_warnings);
        Button saveButton = view.findViewById(R.id.btn_save_budget);

        // setup database helpers
        budgetDao = new BudgetDataBaseAccessHelper(requireContext());
        categoryDao = new CategoryDatabaseAccessHelper(requireContext());
        transactionDao = new TransactionDatabaseAccessHelper(requireContext());

        // get the logged in user's email
        SessionManager prefManager = new SessionManager(requireContext());
        userEmail = prefManager.getUserEmail();

        // load the categories and budgets
        loadCategories();
        loadBudgets();

        // handle the save button click
        saveButton.setOnClickListener(v -> saveBudget());

        return view;
    }


    private void loadCategories() {
        // stop if user is not logged in
        if (userEmail == null) {
            return;
        }
        
        // get all expense categories from the database
        categories = categoryDao.getCategoriesByType(userEmail, "EXPENSE");

        // extract the names of the categories
        List<String> names = new ArrayList<>();
        for (Category c : categories) {
            names.add(c.getName());
        }

        // create the adapter to show categories in the dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                names
        );

        // set the dropdown style and attach to the spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private String getCurrentMonth() {
        // format today's date as year-month for budget tracking
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return sdf.format(new Date());
    }


    private void saveBudget() {
        // check if there are categories available
        if (categories.isEmpty()) {
            Toast.makeText(getContext(), "No categories available", Toast.LENGTH_SHORT).show();
            return;
        }

        // get the amount the user entered
        String amountStr = budgetAmountEditText.getText().toString();
        if (amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Enter budget amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // convert the amount to a number and get the selected category
        double amount = Double.parseDouble(amountStr);
        Category selectedCategory = categories.get(categorySpinner.getSelectedItemPosition());

        // get the current month
        String month = getCurrentMonth();

        // create a new budget object with the data
        Budget budget = new Budget();
        budget.setCategoryId(selectedCategory.getId());
        budget.setLimitAmount(amount);
        budget.setUserEmail(userEmail);
        budget.setMonth(month);

        // save the budget to the database
        budgetDao.insertBudget(budget);

        // clear the input and refresh the list
        budgetAmountEditText.setText("");
        loadBudgets();
    }

    private void loadBudgets() {
        // stop if user is not logged in
        if (userEmail == null) {
            return;
        }

        // get all expense categories and create a quick lookup map
        categories = categoryDao.getCategoriesByType(userEmail, "EXPENSE");
        Map<Integer, String> categoryMap = new HashMap<>();
        for (Category c : categories) {
            categoryMap.put(c.getId(), c.getName());
        }

        // get all budgets for this user from the database
        List<Budget> budgets = budgetDao.getBudgetsByUser(userEmail);

        // build a list of budget information to display
        List<String> displayList = new ArrayList<>();
        
        // collect warnings to show in the warning report
        List<BudgetWarningAdapter.BudgetWarning> warnings = new ArrayList<>();

        for (Budget b : budgets) {
            // get the category name
            String categoryName = categoryMap.getOrDefault(b.getCategoryId(), "Unknown Category");

            // calculate how much money has been spent this month for this category
            double spent = transactionDao.getTotalExpensesForCategoryAndMonth(
                    userEmail,
                    b.getCategoryId(),
                    getCurrentMonth()
            );

            // calculate how much budget is left
            double remaining = b.getLimitAmount() - spent;

            // add the budget info to the display list
            displayList.add(
                    "Category: " + categoryName +
                            " | Limit: " + b.getLimitAmount()
            );

            // check if the spending has reached 50% of the budget limit
            if (spent >= b.getLimitAmount() * 0.5) {
                // add this as a warning to the warnings list
                warnings.add(new BudgetWarningAdapter.BudgetWarning(
                        categoryName,
                        b.getLimitAmount(),
                        spent
                ));
            }
        }

        // show all the budgets in the list view 
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                displayList
        );
        budgetsListView.setAdapter(adapter);

        // display the warnings in the warning report section
        displayBudgetWarnings(warnings);
    }

    // display budget warnings in a dedicated warning report instead of toast messages
    private void displayBudgetWarnings(List<BudgetWarningAdapter.BudgetWarning> warnings) {
        // create a custom adapter for displaying warnings
        BudgetWarningAdapter warningAdapter = new BudgetWarningAdapter(requireContext(), warnings);
        warningsListView.setAdapter(warningAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // refresh the budget list when this screen is shown
        loadBudgets();
    }
}