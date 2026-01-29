package com.example.project.Course_project.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.R;
import com.example.project.Course_project.activites.LoginActivity;
import com.example.project.Course_project.database.CategoryDatabaseAccessHelper;
import com.example.project.Course_project.models.Category;
import com.example.project.Course_project.utils.SessionManager;
import com.example.project.Course_project.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";

    // theme and period settings
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchDarkMode;
    private RadioGroup radioGroupPeriod;

    // category management ui elements
    private Spinner spinnerCategoryType;
    private Spinner spinnerCategoryList;
    private EditText etCategoryName;
    private Button btnAddCategory;
    private Button btnUpdateCategory;
    private Button btnDeleteCategory;
    private LinearLayout containerCategories;

    // logout button
    private Button btnLogout;

    // database and session helpers
    private SharedPrefManager sharedPrefManager;
    private SessionManager sessionManager;
    private CategoryDatabaseAccessHelper categoryDB;

    // lists for the spinners
    private List<String> categoryTypes = new ArrayList<>();
    private List<Category> categoriesForCurrentType = new ArrayList<>();

    // track the selected category
    private int selectedCategoryId = -1;
    private String selectedType = ""; // default type

    public SettingFragment() {}


    //helper method to safely get user email with logging
    private String getUserEmailSafe() {
        try {
            // try to get email from session manager first
            String email = sessionManager.getUserEmail();
            Log.d(TAG, "UserEmail from SessionManager: " + email);

            // if null, try to get from shared preferences directly
            if (email == null || email.isEmpty()) {
                Log.w(TAG, "SessionManager returned null/empty, trying SharedPreferences");
                SharedPreferences prefs = requireContext().getSharedPreferences("user_session_prefs", 0);
                email = prefs.getString("user_email", null);
                Log.d(TAG, "UserEmail from SharedPreferences: " + email);
            }

            // if still null, check if user is logged in
            if (email == null || email.isEmpty()) {
                Log.e(TAG, "User email is null! IsLoggedIn: " + sessionManager.isLoggedIn());
            }

            return email;
        } catch (Exception e) {
            Log.e(TAG, "Error getting user email", e);
            return null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // create the view from the layout file
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // bind all the ui elements to variables
        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        radioGroupPeriod = view.findViewById(R.id.radioGroupPeriod);

        spinnerCategoryType = view.findViewById(R.id.spinnerCategoryType);
        spinnerCategoryList = view.findViewById(R.id.spinnerCategoryList);
        etCategoryName = view.findViewById(R.id.etCategoryName);
        btnAddCategory = view.findViewById(R.id.btnAddCategory);
        btnUpdateCategory = view.findViewById(R.id.btnUpdateCategory);
        btnDeleteCategory = view.findViewById(R.id.btnDeleteCategory);
        containerCategories = view.findViewById(R.id.containerCategories);

        btnLogout = view.findViewById(R.id.btnLogout);

        // initialize the helper objects
        sharedPrefManager = new SharedPrefManager(requireContext());
        sessionManager = new SessionManager(requireContext());
        categoryDB = new CategoryDatabaseAccessHelper(requireContext());

        // load all the saved settings
        loadTheme();
        loadSummaryPeriod();
        setupCategoryTypeSpinner();

        // setup all the button and option listeners
        handleThemeChange();
        handlePeriodChange();
        handleAddCategory();
        handleUpdateCategory();
        handleDeleteCategory();
        handleLogout();

        return view;
    }

    // theme
    private void loadTheme() {
        // get the saved theme and apply it to the switch
        String theme = sharedPrefManager.getTheme();
        switchDarkMode.setChecked(theme.equals("dark"));
    }

    private void handleThemeChange() {
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // apply dark mode if switch is on, light mode if off
            if (isChecked) {
                sharedPrefManager.setTheme("dark");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                sharedPrefManager.setTheme("light");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    // summary period 
    private void loadSummaryPeriod() {
        // get the saved report period and select it in the radio group
        String period = sharedPrefManager.getDefaultPeriod();

        switch (period) {
            case "daily":
                radioGroupPeriod.check(R.id.radioDaily);
                break;
            case "weekly":
                radioGroupPeriod.check(R.id.radioWeekly);
                break;
            default:
                radioGroupPeriod.check(R.id.radioMonthly);
        }
    }

    private void handlePeriodChange() {
        radioGroupPeriod.setOnCheckedChangeListener((group, checkedId) -> {
            // save the selected report period
            if (checkedId == R.id.radioDaily) {
                sharedPrefManager.setDefaultPeriod("daily");
            } else if (checkedId == R.id.radioWeekly) {
                sharedPrefManager.setDefaultPeriod("weekly");
            } else if (checkedId == R.id.radioWeekly) {
                sharedPrefManager.setDefaultPeriod("monthly");
            } else {
                sharedPrefManager.setDefaultPeriod("yearly");
            }
        });
    }

    // categories - type spinner
    private void setupCategoryTypeSpinner() {
        try {
            // define the category type options
            String[] typeOptions = {"-- Select Type --", "INCOME", "EXPENSE"};

            // create adapter for the dropdown
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, typeOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategoryType.setAdapter(adapter);

            // handle when user selects a type
            spinnerCategoryType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // set the selected type and load categories for it
                    if (position == 1) {
                        selectedType = "INCOME";
                        Log.d(TAG, "Type selected: INCOME");
                    } else if (position == 2) {
                        selectedType = "EXPENSE";
                        Log.d(TAG, "Type selected: EXPENSE");
                    } else {
                        selectedType = "";
                        return;
                    }
                    selectedCategoryId = -1;
                    etCategoryName.setText("");
                    loadCategoriesForType();
                    setupCategoryListSpinner();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedType = "";
                    selectedCategoryId = -1;
                    etCategoryName.setText("");
                }
            });

            // set default selection to income
            spinnerCategoryType.setSelection(1);

            Log.d(TAG, "Category type spinner setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up category type spinner", e);
        }
    }

    // categories - list spinner
    private void loadCategoriesForType() {
        try {
            // clear the old categories
            containerCategories.removeAllViews();
            categoriesForCurrentType.clear();

            // if no type is selected, stop here
            if (selectedType.isEmpty()) {
                Log.d(TAG, "No type selected, skipping load");
                return;
            }

            // get the user's email
            String userEmail = getUserEmailSafe();
            Log.d(TAG, "Loading categories for type: " + selectedType + ", user: " + userEmail);

            // check if we have a valid email
            if (userEmail == null || userEmail.isEmpty()) {
                Log.e(TAG, "User email is null or empty - cannot load categories");
                return;
            }

            // get all categories from the database
            List<Category> categories = categoryDB.getCategoriesByType(userEmail, selectedType);

            if (categories == null) {
                Log.e(TAG, "Categories list is null");
                categories = new ArrayList<>();
            }

            categoriesForCurrentType.addAll(categories);

            // display all categories as text views
            for (Category category : categories) {
                TextView tv = new TextView(getContext());
                tv.setText("• " + category.getName());
                tv.setPadding(8, 8, 8, 8);
                containerCategories.addView(tv);
            }

            Log.d(TAG, "Categories loaded: " + categoriesForCurrentType.size());
        } catch (Exception e) {
            Log.e(TAG, "Error loading categories for type", e);
        }
    }

    private void setupCategoryListSpinner() {
        try {
            // build a list of category names for the dropdown
            List<String> categoryOptions = new ArrayList<>();
            categoryOptions.add("-- Select Category --");
            for (Category category : categoriesForCurrentType) {
                categoryOptions.add(category.getName());
            }

            // create adapter for the dropdown
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, categoryOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategoryList.setAdapter(adapter);

            // handle when user selects a category
            spinnerCategoryList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // find the category and populate the text field
                    if (position > 0) {
                        String selectedName = categoryOptions.get(position);
                        for (Category category : categoriesForCurrentType) {
                            if (category.getName().equalsIgnoreCase(selectedName)) {
                                selectedCategoryId = category.getId();
                                etCategoryName.setText(selectedName);
                                Log.d(TAG, "Category selected: " + selectedName + " (ID: " + selectedCategoryId + ")");
                                break;
                            }
                        }
                    } else {
                        selectedCategoryId = -1;
                        etCategoryName.setText("");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedCategoryId = -1;
                    etCategoryName.setText("");
                }
            });

            Log.d(TAG, "Category list spinner setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up category list spinner", e);
        }
    }

    // add category 
    private void handleAddCategory() {
        btnAddCategory.setOnClickListener(v -> {
            try {
                // get the category name and validate it
                String name = etCategoryName.getText().toString().trim();
                Log.d(TAG, "Adding category: " + name + ", type: " + selectedType);

                // check that a type is selected
                if (selectedType.isEmpty()) {
                    Toast.makeText(getContext(), "Please select a category type first", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check that the name is valid
                if (TextUtils.isEmpty(name) || name.length() < 3) {
                    etCategoryName.setError("Enter a valid category name (3-20 characters)");
                    return;
                }

                // get the user's email
                String userEmail = getUserEmailSafe();
                Log.d(TAG, "User email: " + userEmail);

                if (userEmail == null || userEmail.isEmpty()) {
                    Log.e(TAG, "User email is null when adding category");
                    Toast.makeText(getContext(), "Error: Unable to get user email. Please logout and login again.", Toast.LENGTH_LONG).show();
                    return;
                }

                // create the category and save it
                Category category = new Category(name, selectedType, userEmail);
                boolean success = categoryDB.insertCategory(category);

                if (success) {
                    Toast.makeText(getContext(), "Category added successfully", Toast.LENGTH_SHORT).show();
                    etCategoryName.setText("");
                    selectedCategoryId = -1;
                    loadCategoriesForType();
                    setupCategoryListSpinner();
                    Log.d(TAG, "Category added successfully: " + name);
                } else {
                    Log.e(TAG, "Failed to insert category");
                    Toast.makeText(getContext(), "Error adding category - may already exist", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception when adding category", e);
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // update category
    private void handleUpdateCategory() {
        btnUpdateCategory.setOnClickListener(v -> {
            try {
                // check that a category is selected
                if (selectedCategoryId == -1) {
                    Toast.makeText(getContext(), "Please select a category to update", Toast.LENGTH_SHORT).show();
                    return;
                }

                // validate the new name
                String newName = etCategoryName.getText().toString().trim();
                if (TextUtils.isEmpty(newName) || newName.length() < 3) {
                    etCategoryName.setError("Enter a valid category name (3-20 characters)");
                    return;
                }

                // get the user's email
                String userEmail = getUserEmailSafe();
                if (userEmail == null || userEmail.isEmpty()) {
                    Toast.makeText(getContext(), "Error: Unable to get user email. Please logout and login again.", Toast.LENGTH_LONG).show();
                    return;
                }

                // create the updated category and save it
                Category category = new Category(newName, selectedType, userEmail);
                category.setId(selectedCategoryId);
                boolean success = categoryDB.updateCategory(category);

                if (success) {
                    Toast.makeText(getContext(), "Category updated successfully", Toast.LENGTH_SHORT).show();
                    etCategoryName.setText("");
                    selectedCategoryId = -1;
                    loadCategoriesForType();
                    setupCategoryListSpinner();
                    Log.d(TAG, "Category updated: " + newName);
                } else {
                    Toast.makeText(getContext(), "Error updating category", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception when updating category", e);
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // delete category
    private void handleDeleteCategory() {
        btnDeleteCategory.setOnClickListener(v -> {
            try {
                // check that a category is selected
                if (selectedCategoryId == -1) {
                    Toast.makeText(getContext(), "Please select a category to delete", Toast.LENGTH_SHORT).show();
                    return;
                }

                // get the user's email
                String userEmail = getUserEmailSafe();
                if (userEmail == null || userEmail.isEmpty()) {
                    Toast.makeText(getContext(), "Error: Unable to get user email. Please logout and login again.", Toast.LENGTH_LONG).show();
                    return;
                }

                // delete the category from the database
                boolean success = categoryDB.deleteCategory(selectedCategoryId, userEmail);

                if (success) {
                    Toast.makeText(getContext(), "Category deleted successfully", Toast.LENGTH_SHORT).show();
                    etCategoryName.setText("");
                    selectedCategoryId = -1;
                    loadCategoriesForType();
                    setupCategoryListSpinner();
                    Log.d(TAG, "Category deleted: ID=" + selectedCategoryId);
                } else {
                    Toast.makeText(getContext(), "Error deleting category", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception when deleting category", e);
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // logout 
    private void handleLogout() {
        btnLogout.setOnClickListener(v -> {
            // clear the session and go back to login screen
            sessionManager.logout();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
    }
}