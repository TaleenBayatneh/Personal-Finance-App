package com.example.project.Course_project.activites;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.project.R;
import com.example.project.Course_project.database.UserDatabaseAccessHelper;
import com.example.project.Course_project.utils.SharedPrefManager;
import com.example.project.Course_project.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    //UI components for user input
    private EditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;
    private Button loginButton, signupButton, passwordToggleButton;

    private UserDatabaseAccessHelper userDao;//database helper for user queries
    private boolean isPasswordVisible = false;//flag to track password visibility state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //get stored theme preference
        SharedPrefManager pref = new SharedPrefManager(this);
        String theme = pref.getTheme();

        //apply saved theme dark or light mode
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
        setContentView(R.layout.activity_login2);

        //initialize views
        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        rememberMeCheckBox = findViewById(R.id.cb_remember_me);
        loginButton = findViewById(R.id.btn_login);
        signupButton = findViewById(R.id.btn_signup);
        passwordToggleButton = findViewById(R.id.btn_password_toggle);

        //initialize database helper
        userDao = new UserDatabaseAccessHelper(this);

        //restore saved email if remember me was checked previously
        SharedPrefManager prefManager = new SharedPrefManager(this);
        String savedEmail = prefManager.getRememberedEmail();
        if (savedEmail != null) {
            emailEditText.setText(savedEmail);
            rememberMeCheckBox.setChecked(true);
        }

        //set login button click listener
        loginButton.setOnClickListener(v -> loginUser());

        //set signup button to navigate to signup screen
        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        //set password visibility toggle button
        passwordToggleButton.setOnClickListener(v -> togglePasswordVisibility());
    }

    //toggle password visibility between visible and hidden
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

    //handle user login authentication
    private void loginUser() {
        //get email and password from input fields
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        //check if email field is empty
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            return;
        }

        //check if password field is empty
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            return;
        }

        //verify email and password combination in database
        boolean validLogin = userDao.checkLogin(email, password);

        if (validLogin) {
            //create session for logged-in user
            SessionManager prefManager =
                    new SessionManager(this);
            SharedPrefManager sharedPrefManager = new SharedPrefManager(this);

            //store user session with email
            prefManager.createLoginSession(email);

            //save email if remember me checkbox is checked
            if (rememberMeCheckBox.isChecked()) {
                sharedPrefManager.saveRememberedEmail(email);
            } else {
                sharedPrefManager.clearRememberedEmail();
            }

            //navigate to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            //show error message for invalid email or password
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }
}