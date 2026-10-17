USE financial_records;

INSERT INTO transactions (type, description, category, amount, transaction_date, payment_method, notes)
VALUES
    ('INCOME', 'Monthly salary', 'Salary', 25000.00, '2025-01-05', 'Bank Transfer', 'Primary salary deposit'),
    ('EXPENSE', 'Groceries', 'Groceries', 4500.00, '2025-01-08', 'UPI', 'Weekly shopping'),
    ('EXPENSE', 'Train pass', 'Transport', 1200.00, '2025-01-10', 'Card', 'Monthly commute'),
    ('INCOME', 'Freelance project', 'Freelance', 6000.00, '2025-01-15', 'Bank Transfer', 'Website work'),
    ('EXPENSE', 'Electricity bill', 'Bills', 2200.00, '2025-01-18', 'Bank Transfer', 'Utilities');
