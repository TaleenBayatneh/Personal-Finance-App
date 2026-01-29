package com.example.project.Course_project.activites;

import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.example.project.R;
import com.example.project.Course_project.utils.SessionManager;
import com.example.project.Course_project.utils.SharedPrefManager;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    //app bar configuration for navigation drawer
    private AppBarConfiguration mAppBarConfiguration;
    //view binding for activity layout
    private ActivityMainBinding binding;

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

        //inflate layout using view binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //set up toolbar as action bar
        setSupportActionBar(binding.appBarMain.toolbar);

        //initialize drawer layout and navigation view
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        //configure all top level navigation destinations
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_Income, R.id.nav_Expenses, R.id.nav_Budget, R.id.nav_Setting, R.id.nav_Profile)
                .setOpenableLayout(drawer)
                .build();
        //set up navigation controller
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //get reference to navigation header view
        NavigationView navigationView2 = findViewById(R.id.nav_view);
        View headerView = navigationView2.getHeaderView(0);

        //display logged in user email in navigation header
        TextView tvUserEmail = headerView.findViewById(R.id.tvUserEmail);
        SessionManager sessionManager = new SessionManager(this);
        String userEmail = sessionManager.getUserEmail();
        tvUserEmail.setText(userEmail);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu items into action bar
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        //handle back navigation with drawer support
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}