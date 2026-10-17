package com.financetracker.records;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.financetracker.records.model.TransactionType;
import com.financetracker.records.util.CategoryConstants;
import org.junit.jupiter.api.Test;

class FinancialRecordsApplicationTests {

    @Test
    void categoriesContainExpectedDefaults() {
        assertTrue(CategoryConstants.EXPENSE_CATEGORIES.contains("Food"));
        assertTrue(CategoryConstants.INCOME_CATEGORIES.contains("Salary"));
        assertFalse(CategoryConstants.ALL_CATEGORIES.isEmpty());
    }

    @Test
    void transactionTypeIncludesIncomeAndExpense() {
        assertTrue(TransactionType.INCOME.name().equals("INCOME"));
        assertTrue(TransactionType.EXPENSE.name().equals("EXPENSE"));
    }
}
