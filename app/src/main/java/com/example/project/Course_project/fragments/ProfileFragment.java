package com.example.project.Course_project.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project.R;
import com.example.project.Course_project.database.UserDatabaseAccessHelper;
import com.example.project.Course_project.models.User;
import com.example.project.Course_project.utils.SessionManager;


public class ProfileFragment extends Fragment {

    // ui input fields for user information
    private EditText etEmail, etFirstName, etLastName, etCurrentPassword, etNewPassword, etConfirmPassword;
    // buttons for saving and canceling
    private Button btnSave, btnCancel;
    // database and session helpers
    private UserDatabaseAccessHelper userDb;
    private SessionManager sessionManager;
    // current logged in user
    private User currentUser;

    public ProfileFragment() {
        // empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // create the view from the layout file
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // setup database and session helpers
        userDb = new UserDatabaseAccessHelper(getContext());
        sessionManager = new SessionManager(getContext());

        // find all the input fields and buttons on the screen
        etEmail = view.findViewById(R.id.etEmail);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);

        // load the user's current profile data
        loadUserProfile();

        // setup click listeners for the buttons
        btnSave.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> clearForm());

        return view;
    }

    ///////////
    // load the current user's profile data from the database
    ///////////
    private void loadUserProfile() {
        try {
            // get the email of the logged in user
            String email = sessionManager.getUserEmail();
            if (email != null && !email.isEmpty()) {
                // fetch the user from the database
                currentUser = userDb.getUserByEmail(email);
                if (currentUser != null) {
                    // display the user's information in the fields
                    etEmail.setText(currentUser.getEmail());
                    etFirstName.setText(currentUser.getFirstName() != null ? currentUser.getFirstName() : "");
                    etLastName.setText(currentUser.getLastName() != null ? currentUser.getLastName() : "");
                    // password field stays empty for security - user must enter current password
                } else {
                    Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    ///////////
    // save the updated profile information to the database
    ///////////
    private void saveProfile() {
        try {
            // get and validate the current password
            String currentPassword = etCurrentPassword.getText().toString().trim();
            if (currentPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your current password to confirm changes", Toast.LENGTH_SHORT).show();
                return;
            }

            // check if the current password is correct
            if (!currentUser.getPassword().equals(currentPassword)) {
                Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
                etCurrentPassword.setText("");
                return;
            }

            // get the new profile values from the fields
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // check that first and last names are not empty
            if (firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(getContext(), "First name and last name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // if user is changing password, validate it
            if (!newPassword.isEmpty()) {
                // check if the password meets all requirements
                if (!isValidPassword(newPassword)) {
                    etNewPassword.setError("Password must be 6–12 characters and contain uppercase, lowercase, and number");
                    Toast.makeText(getContext(), "Password must be 6–12 characters and contain uppercase, lowercase, and number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check that passwords match
                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(getContext(), "New password and confirm password do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // set the new password
                currentUser.setPassword(newPassword);
            }

            // update the user's name
            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);

            // save the changes to the database
            boolean success = userDb.updateUser(currentUser);
            if (success) {
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                clearForm();
                loadUserProfile(); // reload to show updated data
            } else {
                Toast.makeText(getContext(), "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    ///////////
    // clear all input fields to their default state
    ///////////
    private void clearForm() {
        // clear all password fields
        etCurrentPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
        // reload to reset the name fields
        loadUserProfile();
    }
    ////////////////////////
    // password conditions:
    // 6-12 characters
    // at least one uppercase, one lowercase, and one digit
    ///////////////////////
    private boolean isValidPassword(String password) {
        // check if password length is between 6 and 12 characters
        if (password.length() < 6 || password.length() > 12) {
            return false;
        }

        // flags to track what characters the password contains
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        // loop through each character in the password
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }

        // password is valid only if it has all required character types
        return hasUpper && hasLower && hasDigit;
    }
}