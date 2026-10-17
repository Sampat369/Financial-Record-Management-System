export type TransactionType = 'INCOME' | 'EXPENSE';

export interface Transaction {
  id?: number;
  type: TransactionType;
  description: string;
  category: string;
  amount: number;
  transactionDate: string;
  paymentMethod?: string;
  notes?: string;
  createdAt?: string;
}

export interface DashboardSummary {
  totalIncome: number;
  totalExpenditure: number;
  currentBalance: number;
  currentMonthIncome: number;
  currentMonthExpenditure: number;
  transactionCount: number;
  monthlyIncomeVsExpense: Array<{ month: string; income: number; expense: number }>;
  categoryWiseExpenditure: Record<string, number>;
  recentTransactions: Transaction[];
}
