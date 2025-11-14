import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatListModule } from '@angular/material/list';
import { UserService } from '../../services/user.service';
import { AccountService } from '../../services/account.service';
import { TransactionService } from '../../services/transaction.service';
import { User } from '../../models/user.model';
import { Account } from '../../models/account.model';
import { Transaction } from '../../models/transaction.model';

@Component({
  selector: 'app-dashboard',
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatGridListModule,
    MatListModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  currentUser: User | null = null;
  currentAccount: Account | null = null;
  recentTransactions: Transaction[] = [];
  isLoading = true;
  transactionCount = 0;

  constructor(
    private userService: UserService,
    private accountService: AccountService,
    private transactionService: TransactionService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.currentUser = this.userService.getCurrentUser();
    
    if (!this.currentUser) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadDashboardData();
  }

  loadDashboardData() {
    this.isLoading = true;

    // Load account information
    this.accountService.getAccountByUserId(this.currentUser!.id!).subscribe({
      next: (account) => {
        this.currentAccount = account;
        this.loadTransactionData();
      },
      error: (error) => {
        this.snackBar.open('Failed to load account information', 'Close', { duration: 3000 });
        this.router.navigate(['/account-setup']);
      }
    });
  }

  loadTransactionData() {
    if (!this.currentAccount) return;

    // Load recent transactions
    this.transactionService.getRecentTransactions(this.currentAccount.upiId, 5).subscribe({
      next: (transactions) => {
        this.recentTransactions = transactions;
        this.loadTransactionCount();
      },
      error: (error) => {
        console.error('Failed to load recent transactions:', error);
        this.isLoading = false;
      }
    });
  }

  loadTransactionCount() {
    if (!this.currentAccount) return;

    this.transactionService.getTransactionCount(this.currentAccount.upiId).subscribe({
      next: (count) => {
        this.transactionCount = count;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Failed to load transaction count:', error);
        this.isLoading = false;
      }
    });
  }

  refreshBalance() {
    if (!this.currentAccount) return;

    this.accountService.refreshCurrentAccountBalance().subscribe({
      next: (balanceResponse) => {
        this.snackBar.open('Balance refreshed successfully', 'Close', { duration: 2000 });
      },
      error: (error) => {
        this.snackBar.open('Failed to refresh balance', 'Close', { duration: 3000 });
      }
    });
  }

  navigateToTransfer() {
    this.router.navigate(['/transfer']);
  }

  navigateToHistory() {
    this.router.navigate(['/history']);
  }

  navigateToProfile() {
    this.router.navigate(['/profile']);
  }

  getTransactionDisplayInfo(transaction: Transaction): { amount: number, type: 'sent' | 'received', counterparty: string } {
    const isSent = transaction.senderUpiId === this.currentAccount?.upiId;
    return {
      amount: transaction.amount,
      type: isSent ? 'sent' : 'received',
      counterparty: isSent ? transaction.receiverUpiId : transaction.senderUpiId
    };
  }

  getTransactionIcon(type: 'sent' | 'received'): string {
    return type === 'sent' ? 'arrow_upward' : 'arrow_downward';
  }

  getTransactionColor(type: 'sent' | 'received'): string {
    return type === 'sent' ? 'warn' : 'primary';
  }
}
