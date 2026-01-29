package com.example.project.Course_project.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.R;
import com.example.project.Course_project.database.TransactionDatabaseAccessHelper;
import com.example.project.Course_project.models.Transaction;
import com.example.project.Course_project.utils.SessionManager;
import com.example.project.Course_project.utils.SharedPrefManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/*
    HomeFragment displays financial summary with charts and reports.
    Provides dashboard with income/expense overview, visualizations, and period-based reports.
 */

public class HomeFragment extends Fragment {

    // text views for displaying financial summary
    private TextView incomeTextView, expensesTextView, balanceTextView;
    // text views for displaying reports
    private TextView reportTitleTextView, reportDetailsTextView;
    // charts for visualizing data
    private PieChart pieChart;
    private BarChart barChart;

    // database and session helpers
    private TransactionDatabaseAccessHelper transactionDao;
    private SessionManager sessionManager;
    private SharedPrefManager sharedPrefManager;
    // user and period information
    private String userEmail;
    private String selectedPeriod = "Monthly";

    public HomeFragment() {
        // empty public constructor
    }

    //load period from settings/shared preferences
    private void loadPeriodFromSettings() {
        try {
            // get the report period that was chosen in settings
            String period = sharedPrefManager.getDefaultPeriod();
            if (period != null && !period.isEmpty()) {
                // capitalize first letter for display
                selectedPeriod = period.substring(0, 1).toUpperCase() + period.substring(1);
            } else {
                selectedPeriod = "Monthly"; // default -> monthly
            }
        } catch (Exception e) {
            selectedPeriod = "Monthly"; // fallback to monthly if error
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // create the view from the layout file 'inflate'
        View view = inflater.inflate(R.layout.fragment_home2, container, false);

        // find all the text views and charts on the screen
        incomeTextView = view.findViewById(R.id.tv_income);
        expensesTextView = view.findViewById(R.id.tv_expenses);
        balanceTextView = view.findViewById(R.id.tv_balance);
        reportTitleTextView = view.findViewById(R.id.tv_report_title);
        reportDetailsTextView = view.findViewById(R.id.tv_report_details);
        pieChart = view.findViewById(R.id.pie_chart);
        barChart = view.findViewById(R.id.bar_chart);

        // setup database and session helpers
        transactionDao = new TransactionDatabaseAccessHelper(requireContext());
        sessionManager = new SessionManager(requireContext());
        sharedPrefManager = new SharedPrefManager(requireContext());
        userEmail = sessionManager.getUserEmail();

        // load the period from settings
        loadPeriodFromSettings();

        // load all the data for the dashboard
        loadAllData();

        return view;
    }

    
    //load all data: reports, summary, and charts
    private void loadAllData() {
        // stop if user is not logged in
        if (userEmail == null || userEmail.isEmpty()) {
            showEmptyState();
            return;
        }

        // refresh the period from settings
        loadPeriodFromSettings();

        // load all the data for the dashboard
        loadSummary();
        loadReportDetails();
        loadIncomeVsExpenseChart();
        loadMonthlyExpensesChart();
    }

    ////////////
    // display financial summary (total income, expenses, balance)
    ///////////
    private void loadSummary() {
        try {
            // calculate totals for income and expenses
            double totalIncome = transactionDao.getTotalAmountByType(userEmail, "INCOME");
            double totalExpenses = transactionDao.getTotalAmountByType(userEmail, "EXPENSE");
            double balance = totalIncome - totalExpenses;

            // display the totals
            incomeTextView.setText(String.format("Total Income: $%.2f", totalIncome));
            expensesTextView.setText(String.format("Total Expenses: $%.2f", totalExpenses));
            
            String balanceText = String.format("Balance: $%.2f", balance);
            balanceTextView.setText(balanceText);
            
            // color balance green if positive, red if negative
            if (balance >= 0) {
                balanceTextView.setTextColor(Color.rgb(60,168,38));
            } else {
                balanceTextView.setTextColor(Color.rgb(178,8,8));
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading summary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    ////////////
    // load pie chart showing income vs expenses for current month
    ////////////
    private void loadIncomeVsExpenseChart() {
        try {
            // get current month
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
            String currentMonth = monthFormat.format(new Date());

            // get all income and expense transactions
            List<Transaction> allIncome = transactionDao.getTransactionsByType(userEmail, "INCOME");
            List<Transaction> allExpenses = transactionDao.getTransactionsByType(userEmail, "EXPENSE");

            // group income transactions by month
            Map<String, Float> monthlyIncome = groupTransactionsByMonth(allIncome);
            // group expense transactions by month
            Map<String, Float> monthlyExpenses = groupTransactionsByMonth(allExpenses);

            // get current month totals
            double currentMonthIncome = monthlyIncome.getOrDefault(currentMonth, 0f);
            double currentMonthExpenses = monthlyExpenses.getOrDefault(currentMonth, 0f);

            // create entries for the pie chart
            ArrayList<PieEntry> entries = new ArrayList<>();
            
            if (currentMonthIncome > 0) {
                entries.add(new PieEntry((float) currentMonthIncome, "Income"));
            }
            if (currentMonthExpenses > 0) {
                entries.add(new PieEntry((float) currentMonthExpenses, "Expenses"));
            }

            // show "no data" if there are no transactions
            if (entries.isEmpty()) {
                entries.add(new PieEntry(1, "No Data"));
            }

            // create the dataset and configure the pie chart
            PieDataSet dataSet = new PieDataSet(entries, "Income vs Expenses (" + currentMonth + ")");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            dataSet.setValueTextSize(12f);

            PieData data = new PieData(dataSet);
            pieChart.setData(data);
            pieChart.setDescription(null);
            pieChart.setTouchEnabled(true);
            pieChart.animateY(1000);
            pieChart.invalidate();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading pie chart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    ////////////
    // load bar chart showing monthly expenses distribution
    ///////////
    private void loadMonthlyExpensesChart() {
        try {
            // get all expenses from the database
            List<Transaction> allExpenses = transactionDao.getTransactionsByType(userEmail, "EXPENSE");
            
            // group expenses by month
            Map<String, Float> monthlyTotals = groupTransactionsByMonth(allExpenses);

            // create bar entries from the monthly totals
            ArrayList<BarEntry> entries = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();
            int index = 0;

            for (Map.Entry<String, Float> entry : monthlyTotals.entrySet()) {
                entries.add(new BarEntry(index, entry.getValue()));
                labels.add(entry.getKey());
                index++;
            }

            // show "no data" if there are no expenses
            if (entries.isEmpty()) {
                entries.add(new BarEntry(0, 0));
                labels.add("No Data");
            }

            // create the dataset and configure the bar chart
            BarDataSet dataSet = new BarDataSet(entries, "Monthly Expenses");
            dataSet.setColor(Color.parseColor("#FF6B6B"));
            dataSet.setValueTextSize(12f);

            BarData data = new BarData(dataSet);
            barChart.setData(data);
            barChart.setDescription(null);
            barChart.getXAxis().setDrawGridLines(false);
            barChart.animateY(1000);
            barChart.invalidate();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading bar chart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    
    // generate and display period-based report (daily, weekly, monthly, yearly)
    private void loadReportDetails() {
        try {
            // get all transactions for this user
            List<Transaction> allTransactions = transactionDao.getAllTransactions(userEmail);
            
            // build the report text
            StringBuilder reportText = new StringBuilder();
            reportText.append(selectedPeriod).append(" Report\n\n");

            // generate the report based on the selected period
            switch (selectedPeriod) {
                case "Daily":
                    reportText.append(generateDailyReport(allTransactions));
                    break;
                case "Weekly":
                    reportText.append(generateWeeklyReport(allTransactions));
                    break;
                case "Monthly":
                    reportText.append(generateMonthlyReport(allTransactions));
                    break;
                case "Yearly":
                    reportText.append(generateYearlyReport(allTransactions));
                    break;
            }

            // display the report
            reportTitleTextView.setText(selectedPeriod + " Financial Report");
            reportDetailsTextView.setText(reportText.toString());
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    ////////////
    // generate daily report (today's transactions)
    ///////////
    private String generateDailyReport(List<Transaction> transactions) {
        // get today's date
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        // totals for today
        double dailyIncome = 0;
        double dailyExpenses = 0;
        int transactionCount = 0;

        // loop through all transactions and count today's
        for (Transaction t : transactions) {
            if (t.getDate().startsWith(today)) {
                if ("INCOME".equals(t.getType())) {
                    dailyIncome += t.getAmount();
                } else {
                    dailyExpenses += t.getAmount();
                }
                transactionCount++;
            }
        }

        return String.format("Date: %s\nTransactions: %d\nIncome: $%.2f\nExpenses: $%.2f\nNet: $%.2f",
                today, transactionCount, dailyIncome, dailyExpenses, (dailyIncome - dailyExpenses));
    }

    ////////////
    // generate weekly report (current week's transactions)
    ///////////
    private String generateWeeklyReport(List<Transaction> transactions) {
        // get the start of the current week
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date weekStart = calendar.getTime();
        
        // get the end of the current week
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        Date weekEnd = calendar.getTime();

        // totals for the week
        double weeklyIncome = 0;
        double weeklyExpenses = 0;
        int transactionCount = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // loop through transactions and count this week's
        for (Transaction t : transactions) {
            try {
                Date transDate = sdf.parse(t.getDate());
                if (transDate != null && transDate.after(weekStart) && transDate.before(weekEnd)) {
                    if ("INCOME".equals(t.getType())) {
                        weeklyIncome += t.getAmount();
                    } else {
                        weeklyExpenses += t.getAmount();
                    }
                    transactionCount++;
                }
            } catch (Exception e) {
                // skip invalid dates
            }
        }

        return String.format("Week: %s to %s\nTransactions: %d\nIncome: $%.2f\nExpenses: $%.2f\nNet: $%.2f",
                sdf.format(weekStart), sdf.format(weekEnd), transactionCount, weeklyIncome, weeklyExpenses, 
                (weeklyIncome - weeklyExpenses));
    }

    ////////////
    // generate monthly report (current month's transactions)
    ///////////
    private String generateMonthlyReport(List<Transaction> transactions) {
        // get the current month
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String currentMonth = monthFormat.format(new Date());

        // totals for the month
        double monthlyIncome = 0;
        double monthlyExpenses = 0;
        int transactionCount = 0;

        // loop through transactions and count this month's
        for (Transaction t : transactions) {
            if (t.getDate().startsWith(currentMonth)) {
                if ("INCOME".equals(t.getType())) {
                    monthlyIncome += t.getAmount();
                } else {
                    monthlyExpenses += t.getAmount();
                }
                transactionCount++;
            }
        }

        return String.format("Month: %s\nTransactions: %d\nIncome: $%.2f\nExpenses: $%.2f\nNet: $%.2f",
                currentMonth, transactionCount, monthlyIncome, monthlyExpenses, (monthlyIncome - monthlyExpenses));
    }

    ////////////
    // generate yearly report (current year's transactions)
    ///////////
    private String generateYearlyReport(List<Transaction> transactions) {
        // get the current year
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        String currentYear = yearFormat.format(new Date());

        // totals for the year
        double yearlyIncome = 0;
        double yearlyExpenses = 0;
        int transactionCount = 0;

        // loop through transactions and count this year's
        for (Transaction t : transactions) {
            if (t.getDate().startsWith(currentYear)) {
                if ("INCOME".equals(t.getType())) {
                    yearlyIncome += t.getAmount();
                } else {
                    yearlyExpenses += t.getAmount();
                }
                transactionCount++;
            }
        }

        return String.format("Year: %s\nTransactions: %d\nIncome: $%.2f\nExpenses: $%.2f\nNet: $%.2f",
                currentYear, transactionCount, yearlyIncome, yearlyExpenses, (yearlyIncome - yearlyExpenses));
    }

    ////////////
    // group transactions by month for chart display
    ///////////
    private Map<String, Float> groupTransactionsByMonth(List<Transaction> transactions) {
        // map to store monthly totals
        Map<String, Float> monthlyTotals = new HashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

        // loop through each transaction and add to the corresponding month
        for (Transaction t : transactions) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(t.getDate());
                String month = monthFormat.format(date);
                
                // add the amount to the month total
                monthlyTotals.put(month, monthlyTotals.getOrDefault(month, 0f) + (float) t.getAmount());
            } catch (Exception e) {
                // skip invalid dates
            }
        }

        return monthlyTotals;
    }

    
    // show empty state when no user is logged in
    private void showEmptyState() {
        // show zero values for all fields
        incomeTextView.setText("Total Income: $0.00");
        expensesTextView.setText("Total Expenses: $0.00");
        balanceTextView.setText("Balance: $0.00");
        // show message to add transactions
        reportDetailsTextView.setText("No data available. Please add some transactions.");
    }
}