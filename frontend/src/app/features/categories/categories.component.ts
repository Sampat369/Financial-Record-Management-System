import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { defaultExpenseCategories, defaultIncomeCategories } from '../../core/constants/category.constants';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './categories.component.html',
  styleUrl: './categories.component.css'
})
export class CategoriesComponent {
  expenseCategories = defaultExpenseCategories;
  incomeCategories = defaultIncomeCategories;
}
