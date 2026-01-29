package com.example.project.Course_project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.project.R;

import java.util.List;

// custom adapter to display budget warning items in a list view
public class BudgetWarningAdapter extends ArrayAdapter<BudgetWarningAdapter.BudgetWarning> {

    private Context context;
    private List<BudgetWarning> warnings;

    public BudgetWarningAdapter(@NonNull Context context, List<BudgetWarning> warnings) {
        super(context, 0, warnings);
        this.context = context;
        this.warnings = warnings;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // reuse the view if possible, otherwise create a new one
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_budget_warning, parent, false);
        }

        // get the current warning object
        BudgetWarning warning = warnings.get(position);

        // bind the warning data to the view elements
        TextView tvCategory = convertView.findViewById(R.id.tv_warning_category);
        TextView tvStatus = convertView.findViewById(R.id.tv_warning_status);
        TextView tvLimit = convertView.findViewById(R.id.tv_warning_limit);
        TextView tvSpent = convertView.findViewById(R.id.tv_warning_spent);
        TextView tvRemaining = convertView.findViewById(R.id.tv_warning_remaining);
        ProgressBar pbProgress = convertView.findViewById(R.id.pb_warning_progress);

        // set the category name
        tvCategory.setText(warning.getCategoryName());

        // set the warning status percentage
        tvStatus.setText(String.format("%.0f%% used", warning.getUsagePercentage()));

        // set the budget limit
        tvLimit.setText(String.format("$%.2f", warning.getLimitAmount()));

        // set the amount spent
        tvSpent.setText(String.format("$%.2f", warning.getSpentAmount()));

        // set the remaining budget
        tvRemaining.setText(String.format("$%.2f", warning.getRemainingAmount()));

        // set the progress bar value
        pbProgress.setProgress((int) warning.getUsagePercentage());

        return convertView;
    }

    // inner class to represent a budget warning with all necessary details
    public static class BudgetWarning {
        private String categoryName;
        private double limitAmount;
        private double spentAmount;
        private double remainingAmount;
        private double usagePercentage;

        public BudgetWarning(String categoryName, double limitAmount, double spentAmount) {
            this.categoryName = categoryName;
            this.limitAmount = limitAmount;
            this.spentAmount = spentAmount;
            this.remainingAmount = limitAmount - spentAmount;
            this.usagePercentage = (spentAmount / limitAmount) * 100;
        }

        // getters for all warning properties
        public String getCategoryName() {
            return categoryName;
        }

        public double getLimitAmount() {
            return limitAmount;
        }

        public double getSpentAmount() {
            return spentAmount;
        }

        public double getRemainingAmount() {
            return remainingAmount;
        }

        public double getUsagePercentage() {
            return usagePercentage;
        }
    }
}
