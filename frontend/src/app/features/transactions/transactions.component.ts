import { CommonModule, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { defaultExpenseCategories, defaultIncomeCategories } from '../../core/constants/category.constants';
import { Transaction, TransactionType } from '../../core/models/transaction.model';
import { TransactionService } from '../../core/services/transaction.service';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, DatePipe],
  templateUrl: './transactions.component.html',
  styleUrl: './transactions.component.css'
})
export class TransactionsComponent implements OnInit {
  transactions: Transaction[] = [];
  filteredTransactions: Transaction[] = [];
  categories: string[] = [...defaultExpenseCategories, ...defaultIncomeCategories];
  transactionForm: FormGroup;
  editingId: number | null = null;
  selectedTransaction: Transaction | null = null;
  isSubmitting = false;
  deletingId: number | null = null;
  successMessage = '';
  errorMessage = '';
  searchTerm = '';
  typeFilter = '';
  categoryFilter = '';
  startDate = '';
  endDate = '';
  currentPage = 1;
  pageSize = 8;
  sortKey: keyof Transaction | 'transactionDate' = 'transactionDate';
  sortDirection: 'asc' | 'desc' = 'desc';

  constructor(private fb: FormBuilder, private transactionService: TransactionService) {
    this.transactionForm = this.fb.group({
      type: ['EXPENSE', Validators.required],
      description: ['', [Validators.required, Validators.maxLength(200)]],
      category: ['', Validators.required],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      transactionDate: ['', Validators.required],
      paymentMethod: [''],
      notes: ['']
    });
  }

  ngOnInit(): void {
    this.loadTransactions();
    this.transactionService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (error: unknown) => {
        this.errorMessage = this.getRequestError(error, 'Unable to load transaction categories.');
      }
    });
  }

  loadTransactions(): void {
    this.transactionService.getTransactions({
      description: this.searchTerm,
      type: this.typeFilter,
      category: this.categoryFilter,
      startDate: this.startDate,
      endDate: this.endDate
    }).subscribe({
      next: (transactions) => {
        this.transactions = transactions;
        this.applySortingAndPagination();
      },
      error: (error: unknown) => {
        this.errorMessage = this.getRequestError(error, 'Unable to load transactions.');
      }
    });
  }

  applySortingAndPagination(): void {
    const sorted = [...this.transactions].sort((a, b) => {
      const left = a[this.sortKey] ?? '';
      const right = b[this.sortKey] ?? '';
      const result = left > right ? 1 : left < right ? -1 : 0;
      return this.sortDirection === 'asc' ? result : -result;
    });

    const start = (this.currentPage - 1) * this.pageSize;
    this.filteredTransactions = sorted.slice(start, start + this.pageSize);
  }

  updatePage(page: number): void {
    this.currentPage = page;
    this.applySortingAndPagination();
  }

  sortBy(key: keyof Transaction): void {
    if (this.sortKey === key) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortKey = key;
      this.sortDirection = 'asc';
    }
    this.currentPage = 1;
    this.applySortingAndPagination();
  }

  submitTransaction(): void {
    if (this.transactionForm.invalid) {
      this.transactionForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';
    const payload: Transaction = this.transactionForm.value;
    const wasEditing = this.editingId !== null;

    const request$ = this.editingId !== null
      ? this.transactionService.updateTransaction(this.editingId, payload)
      : this.transactionService.createTransaction(payload);

    request$.subscribe({
      next: () => {
        this.resetForm();
        this.successMessage = wasEditing ? 'Transaction changes saved.' : 'Transaction added.';
        this.loadTransactions();
        this.isSubmitting = false;
      },
      error: (error: unknown) => {
        this.errorMessage = this.getRequestError(error, 'Unable to save this transaction.');
        this.isSubmitting = false;
      }
    });
  }

  resetForm(): void {
    this.transactionForm.reset({
      type: 'EXPENSE',
      description: '',
      category: '',
      amount: null,
      transactionDate: '',
      paymentMethod: '',
      notes: ''
    });
    this.editingId = null;
    this.errorMessage = '';
  }

  startNewTransaction(): void {
    this.resetForm();
    this.successMessage = '';
    document.getElementById('transaction-form-panel')?.scrollIntoView({
      behavior: 'smooth',
      block: 'start'
    });
  }

  viewTransaction(transaction: Transaction): void {
    this.selectedTransaction = transaction;
  }

  editTransaction(transaction: Transaction): void {
    this.editingId = transaction.id ?? null;
    this.successMessage = '';
    this.errorMessage = '';
    this.transactionForm.patchValue({
      type: transaction.type,
      description: transaction.description,
      category: transaction.category,
      amount: transaction.amount,
      transactionDate: transaction.transactionDate,
      paymentMethod: transaction.paymentMethod ?? '',
      notes: transaction.notes ?? ''
    });
    document.getElementById('transaction-form-panel')?.scrollIntoView({
      behavior: 'smooth',
      block: 'start'
    });
  }

  deleteTransaction(id: number | undefined): void {
    if (id === undefined) {
      return;
    }

    const confirmed = window.confirm('Delete this transaction?');
    if (!confirmed) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.deletingId = id;
    this.transactionService.deleteTransaction(id).subscribe({
      next: () => {
        if (this.editingId === id) {
          this.resetForm();
        }
        this.successMessage = 'Transaction deleted.';
        this.deletingId = null;
        this.loadTransactions();
      },
      error: (error: unknown) => {
        this.errorMessage = this.getRequestError(error, 'Unable to delete this transaction.');
        this.deletingId = null;
      }
    });
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value);
  }

  get typeOptions(): TransactionType[] {
    return ['INCOME', 'EXPENSE'];
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.transactions.length / this.pageSize));
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, index) => index + 1);
  }

  getFieldError(controlName: string): string | null {
    const control = this.transactionForm.get(controlName);
    if (!control || !control.touched || !control.errors) {
      return null;
    }

    if (control.hasError('required')) {
      return 'This field is required.';
    }
    if (control.hasError('min')) {
      return 'Amount must be greater than zero.';
    }
    if (control.hasError('maxlength')) {
      return 'Value is too long.';
    }
    return 'Invalid value.';
  }

  private getRequestError(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      const body: unknown = error.error;
      if (typeof body === 'string' && body.trim()) {
        return body;
      }
      if (typeof body === 'object' && body !== null) {
        if ('message' in body && typeof body.message === 'string') {
          return body.message;
        }
        const fieldMessages = Object.values(body).filter(
          (value): value is string => typeof value === 'string'
        );
        if (fieldMessages.length > 0) {
          return fieldMessages.join(' ');
        }
      }
    }
    return fallback;
  }
}
