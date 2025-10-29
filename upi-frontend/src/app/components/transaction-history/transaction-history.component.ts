import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule, MatTabChangeEvent } from '@angular/material/tabs';
import { UserService } from '../../services/user.service';
import { AccountService } from '../../services/account.service';
import { TransactionService } from '../../services/transaction.service';
import { User } from '../../models/user.model';
import { Account } from '../../models/account.model';
import { Transaction, TransactionStatus } from '../../models/transaction.model';

@Component({
  selector: 'app-transaction-history',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatListModule,
    MatChipsModule,
    MatTabsModule
  ],
  templateUrl: './transaction-history.component.html',
  styleUrl: './transaction-history.component.scss'
})
export class TransactionHistoryComponent implements OnInit {
  currentUser: User | null = null;
  currentAccount: Account | null = null;
  allTransactions: Transaction[] = [];
  sentTransactions: Transaction[] = [];
  receivedTransactions: Transaction[] = [];
  filteredTransactions: Transaction[] = [];
  isLoading = true;
  filterForm: FormGroup;
  
  transactionStatuses = Object.values(TransactionStatus);
  selectedTab = 0;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private accountService: AccountService,
    private transactionService: TransactionService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.filterForm = this.fb.group({
      status: [''],
      startDate: [''],
      endDate: [''],
      searchTerm: ['']
    });
  }

  ngOnInit() {
    this.currentUser = this.userService.getCurrentUser();
    this.currentAccount = this.accountService.getCurrentAccount();

    if (!this.currentUser || !this.currentAccount) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadTransactions();
    
    // Subscribe to filter changes
    this.filterForm.valueChanges.subscribe(() => {
      this.applyFilters();
    });
  }

  loadTransactions() {
    this.isLoading = true;

    // Load all transactions
    this.transactionService.getUserTransactionHistory(this.currentAccount!.upiId).subscribe({
      next: (transactions) => {
        this.allTransactions = transactions;
        this.categorizeTransactions();
        this.applyFilters();
        this.isLoading = false;
      },
      error: (error) => {
        this.snackBar.open('Failed to load transaction history', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  categorizeTransactions() {
    const currentUpiId = this.currentAccount!.upiId;
    
    this.sentTransactions = this.allTransactions.filter(
      transaction => transaction.senderUpiId === currentUpiId
    );
    
    this.receivedTransactions = this.allTransactions.filter(
      transaction => transaction.receiverUpiId === currentUpiId
    );
  }

  applyFilters() {
    let transactions = this.getTransactionsForCurrentTab();
    const filters = this.filterForm.value;

    // Filter by status
    if (filters.status) {
      transactions = transactions.filter(t => t.status === filters.status);
    }

    // Filter by date range
    if (filters.startDate) {
      const startDate = new Date(filters.startDate);
      transactions = transactions.filter(t => new Date(t.createdAt) >= startDate);
    }

    if (filters.endDate) {
      const endDate = new Date(filters.endDate);
      endDate.setHours(23, 59, 59, 999); // End of day
      transactions = transactions.filter(t => new Date(t.createdAt) <= endDate);
    }

    // Filter by search term (UPI ID or description)
    if (filters.searchTerm) {
      const searchTerm = filters.searchTerm.toLowerCase();
      transactions = transactions.filter(t => 
        t.senderUpiId.toLowerCase().includes(searchTerm) ||
        t.receiverUpiId.toLowerCase().includes(searchTerm) ||
        (t.description && t.description.toLowerCase().includes(searchTerm)) ||
        t.transactionRef.toLowerCase().includes(searchTerm)
      );
    }

    this.filteredTransactions = transactions;
  }

  getTransactionsForCurrentTab(): Transaction[] {
    switch (this.selectedTab) {
      case 0: return this.allTransactions;
      case 1: return this.sentTransactions;
      case 2: return this.receivedTransactions;
      default: return this.allTransactions;
    }
  }

  onTabChange(event: MatTabChangeEvent) {
    this.selectedTab = event.index;
    this.applyFilters();
  }

  clearFilters() {
    this.filterForm.reset();
  }

  getTransactionDisplayInfo(transaction: Transaction): { 
    amount: number, 
    type: 'sent' | 'received', 
    counterparty: string,
    icon: string,
    color: string 
  } {
    const isSent = transaction.senderUpiId === this.currentAccount?.upiId;
    return {
      amount: transaction.amount,
      type: isSent ? 'sent' : 'received',
      counterparty: isSent ? transaction.receiverUpiId : transaction.senderUpiId,
      icon: isSent ? 'arrow_upward' : 'arrow_downward',
      color: isSent ? 'warn' : 'primary'
    };
  }

  getStatusColor(status: TransactionStatus): string {
    switch (status) {
      case TransactionStatus.SUCCESS: return 'primary';
      case TransactionStatus.FAILED: return 'warn';
      case TransactionStatus.PENDING: return 'accent';
      default: return '';
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
  }

  exportTransactions() {
    // Simple CSV export
    const csvData = this.filteredTransactions.map(t => {
      const info = this.getTransactionDisplayInfo(t);
      return [
        t.transactionRef,
        this.formatDate(t.createdAt),
        info.type,
        info.counterparty,
        t.amount,
        t.status,
        t.description || ''
      ].join(',');
    });

    const csvContent = [
      'Transaction ID,Date,Type,Counterparty,Amount,Status,Description',
      ...csvData
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `transactions_${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  refreshTransactions() {
    this.loadTransactions();
  }

  goToDashboard() {
    this.router.navigate(['/dashboard']);
  }

  makeNewTransfer() {
    this.router.navigate(['/transfer']);
  }
}
