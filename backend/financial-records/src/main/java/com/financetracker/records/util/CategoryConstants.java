package com.financetracker.records.util;

import java.util.List;

public final class CategoryConstants {

    private CategoryConstants() {
    }

    public static final List<String> EXPENSE_CATEGORIES = List.of(
            "Food",
            "Groceries",
            "Transport",
            "Education",
            "Shopping",
            "Rent and Accommodation",
            "Bills",
            "Healthcare",
            "Entertainment",
            "Other"
    );

    public static final List<String> INCOME_CATEGORIES = List.of(
            "Salary",
            "Pocket Money",
            "Scholarship",
            "Freelance",
            "Refund",
            "Other"
    );

    public static final List<String> ALL_CATEGORIES = List.of(
            "Food",
            "Groceries",
            "Transport",
            "Education",
            "Shopping",
            "Rent and Accommodation",
            "Bills",
            "Healthcare",
            "Entertainment",
            "Other",
            "Salary",
            "Pocket Money",
            "Scholarship",
            "Freelance",
            "Refund"
    );
}
