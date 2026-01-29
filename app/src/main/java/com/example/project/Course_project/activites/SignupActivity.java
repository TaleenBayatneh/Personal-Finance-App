package com.example.project.Course_project.activites;

import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.project.R;
import com.example.project.Course_project.database.UserDatabaseAccessHelper;
import com.example.project.Course_project.models.User;
import com.example.project.Course_project.utils.SharedPrefManager;

public class SignupActivity extends AppCompatActivity {

    //UI input fields
    private EditText emailEditText, firstNameEditText, lastNameEditText;
    private EditText passwordEditText, confirmPasswordEditText;
    private Button signupButton, passwordToggleButton, confirmPasswordToggleButton;

    
    private UserDatabaseAccessHelper userDao;//database helper for user operations
    //flags to track password visibility states
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //get stored theme preference from shared preferences
        SharedPrefManager pref = new SharedPrefManager(this);
        String theme = pref.getTheme();

        //apply dark or light theme based on user preference
        if ("dark".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
            );
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
            );
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //initialize database helper
        userDao = new UserDatabaseAccessHelper(this);

        //initialize all ui input fields
        emailEditText = findViewById(R.id.et_email);
        firstNameEditText = findViewById(R.id.et_first_name);
        lastNameEditText = findViewById(R.id.et_last_name);
        passwordEditText = findViewById(R.id.et_password);
        confirmPasswordEditText = findViewById(R.id.et_confirm_password);
        signupButton = findViewById(R.id.btn_signup);
        passwordToggleButton = findViewById(R.id.btn_password_toggle);
        confirmPasswordToggleButton = findViewById(R.id.btn_confirm_password_toggle);
        Button backButton = findViewById(R.id.button);

        //set button click listeners for signup and password visibility
        signupButton.setOnClickListener(v -> registerUser());
        passwordToggleButton.setOnClickListener(v -> togglePasswordVisibility());
        confirmPasswordToggleButton.setOnClickListener(v -> toggleConfirmPasswordVisibility());
        backButton.setOnClickListener(v -> finish());
    }

    //toggle password visibility between visible and hidden state
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            //hide password as dots
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordToggleButton.setText("SHOW");
            isPasswordVisible = false;
        } else {
            //show password as plain text
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordToggleButton.setText("HIDE");
            isPasswordVisible = true;
        }
        //move cursor to end of password field
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

    //toggle confirm password visibility between visible and hidden state
    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            //hide confirm password as dots
            confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            confirmPasswordToggleButton.setText("SHOW");
            isConfirmPasswordVisible = false;
        } else {
            //show confirm password as plain text
            confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            confirmPasswordToggleButton.setText("HIDE");
            isConfirmPasswordVisible = true;
        }
        //move cursor to end of confirm password field
        confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
    }

    //handle user registration with input validation
    private void registerUser() {
        //get all input values from form fields
        String email = emailEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        boolean valid = true;

        //validate email format
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid email address");
            valid = false;
        }

        //validate first name length
        if (firstName.length() < 3 || firstName.length() > 10) {
            firstNameEditText.setError("First name must be 3–10 characters");
            valid = false;
        }

        //validate last name length
        if (lastName.length() < 3 || lastName.length() > 10) {
            lastNameEditText.setError("Last name must be 3–10 characters");
            valid = false;
        }

        //validate password strength and format
        if (!isValidPassword(password)) {
            passwordEditText.setError(
                    "Password must be 6–12 characters and contain uppercase, lowercase, and number"
            );
            valid = false;
        }

        //check if passwords match
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            valid = false;
        }

        //show error if validation failed
        if (!valid) {
            Toast.makeText(this, "Please fix the highlighted errors", Toast.LENGTH_SHORT).show();
            return;
        }

        //create new user object with form data
        User user = new User(email, firstName, lastName, password);

        //insert user into database
        boolean inserted = userDao.insertUser(user);

        //show success or failure message
        if (inserted) {
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
        }
    }

    //validate password strength 6-12 chars with uppercase, lowercase, digit
    private boolean isValidPassword(String password) {
        //check password length is between 6 and 12 characters
        if (password.length() < 6 || password.length() > 12) {
            return false;
        }

        //flags to track character types found
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        //loop through each character in password
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }

        //password is valid if all character types are present
        return hasUpper && hasLower && hasDigit;
    }
}
