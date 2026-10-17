import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardSummary } from '../../core/models/transaction.model';
import { TransactionService } from '../../core/services/transaction.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  summary: DashboardSummary | null = null;
  isLoading = true;

  constructor(private transactionService: TransactionService) {}

  ngOnInit(): void {
    this.loadSummary();
  }

  loadSummary(): void {
    this.transactionService.getDashboardSummary().subscribe({
      next: (data) => {
        this.summary = data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  formatCurrency(value: number | undefined): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value ?? 0);
  }

  chartHeight(value: number): number {
    const max = Math.max(...Object.values(this.summary?.categoryWiseExpenditure ?? {}), 1);
    return (value / max) * 100;
  }

  barHeight(value: number): number {
    const max = Math.max(
      ...this.summary?.monthlyIncomeVsExpense.flatMap((item) => [item.income, item.expense]) ?? [0],
      1
    );
    return value > 0 ? Math.max((value / max) * 100, 4) : 0;
  }
}
