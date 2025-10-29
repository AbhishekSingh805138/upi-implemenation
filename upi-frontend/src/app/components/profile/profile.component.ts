import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { UserService } from '../../services/user.service';
import { AccountService } from '../../services/account.service';
import { TransactionService } from '../../services/transaction.service';
import { User } from '../../models/user.model';
import { Account } from '../../models/account.model';

@Component({
  selector: 'app-profile',
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
    MatTabsModule,
    MatListModule,
    MatDividerModule
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {
  currentUser: User | null = null;
  currentAccount: Account | null = null;
  profileForm: FormGroup;
  isLoading = false;
  isEditing = false;
  transactionStats: any = {};

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private accountService: AccountService,
    private transactionService: TransactionService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.profileForm = this.fb.group({
      fullName: ['', [
        Validators.required, 
        Validators.minLength(2), 
        Validators.maxLength(100)
      ]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [
        Validators.required, 
        Validators.pattern(/^[+]?[0-9]{10,15}$/)
      ]]
    });
  }

  ngOnInit() {
    this.currentUser = this.userService.getCurrentUser();
    this.currentAccount = this.accountService.getCurrentAccount();

    if (!this.currentUser) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadUserData();
    this.loadTransactionStats();
  }

  loadUserData() {
    if (this.currentUser) {
      this.profileForm.patchValue({
        fullName: this.currentUser.fullName,
        email: this.currentUser.email,
        phone: this.currentUser.phone
      });
    }
  }

  loadTransactionStats() {
    if (!this.currentAccount) return;

    // Load transaction statistics
    this.transactionService.getTransactionCount(this.currentAccount.upiId).subscribe({
      next: (count) => {
        this.transactionStats.totalTransactions = count;
      },
      error: (error) => {
        console.error('Failed to load transaction stats:', error);
      }
    });

    // Load recent transactions for additional stats
    this.transactionService.getUserTransactionHistory(this.currentAccount.upiId).subscribe({
      next: (transactions) => {
        const currentUpiId = this.currentAccount!.upiId;
        
        this.transactionStats.sentTransactions = transactions.filter(
          t => t.senderUpiId === currentUpiId
        ).length;
        
        this.transactionStats.receivedTransactions = transactions.filter(
          t => t.receiverUpiId === currentUpiId
        ).length;
        
        this.transactionStats.totalAmount = transactions
          .filter(t => t.senderUpiId === currentUpiId)
          .reduce((sum, t) => sum + t.amount, 0);
        
        this.transactionStats.receivedAmount = transactions
          .filter(t => t.receiverUpiId === currentUpiId)
          .reduce((sum, t) => sum + t.amount, 0);
      },
      error: (error) => {
        console.error('Failed to load transaction history for stats:', error);
      }
    });
  }

  toggleEdit() {
    this.isEditing = !this.isEditing;
    
    if (!this.isEditing) {
      // Reset form if canceling edit
      this.loadUserData();
    }
  }

  onSubmit() {
    if (this.profileForm.valid && this.currentUser) {
      this.isLoading = true;
      
      const updateData = {
        fullName: this.profileForm.value.fullName,
        email: this.profileForm.value.email,
        phone: this.profileForm.value.phone
      };

      this.userService.updateUser(this.currentUser.id!, updateData).subscribe({
        next: (updatedUser) => {
          this.currentUser = updatedUser;
          this.isLoading = false;
          this.isEditing = false;
          this.snackBar.open('Profile updated successfully!', 'Close', { duration: 3000 });
        },
        error: (error) => {
          this.isLoading = false;
          this.snackBar.open(error.message || 'Failed to update profile', 'Close', { duration: 5000 });
        }
      });
    }
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

  copyUpiId() {
    if (this.currentAccount?.upiId) {
      navigator.clipboard.writeText(this.currentAccount.upiId).then(() => {
        this.snackBar.open('UPI ID copied to clipboard!', 'Close', { duration: 2000 });
      }).catch(() => {
        this.snackBar.open('Failed to copy UPI ID', 'Close', { duration: 2000 });
      });
    }
  }

  copyAccountNumber() {
    if (this.currentAccount?.accountNumber) {
      navigator.clipboard.writeText(this.currentAccount.accountNumber).then(() => {
        this.snackBar.open('Account number copied to clipboard!', 'Close', { duration: 2000 });
      }).catch(() => {
        this.snackBar.open('Failed to copy account number', 'Close', { duration: 2000 });
      });
    }
  }

  logout() {
    this.userService.logout();
    this.accountService.clearCurrentAccount();
    this.router.navigate(['/login']);
  }

  goToDashboard() {
    this.router.navigate(['/dashboard']);
  }

  goToTransactionHistory() {
    this.router.navigate(['/history']);
  }

  getErrorMessage(fieldName: string): string {
    const field = this.profileForm.get(fieldName);
    
    if (field?.hasError('required')) {
      return `${this.getFieldDisplayName(fieldName)} is required`;
    }
    
    if (field?.hasError('email')) {
      return 'Please enter a valid email address';
    }
    
    if (field?.hasError('minlength')) {
      const minLength = field.errors?.['minlength'].requiredLength;
      return `${this.getFieldDisplayName(fieldName)} must be at least ${minLength} characters`;
    }
    
    if (field?.hasError('maxlength')) {
      const maxLength = field.errors?.['maxlength'].requiredLength;
      return `${this.getFieldDisplayName(fieldName)} must not exceed ${maxLength} characters`;
    }
    
    if (field?.hasError('pattern')) {
      return 'Please enter a valid phone number';
    }
    
    return '';
  }

  private getFieldDisplayName(fieldName: string): string {
    const displayNames: { [key: string]: string } = {
      fullName: 'Full Name',
      email: 'Email',
      phone: 'Phone'
    };
    return displayNames[fieldName] || fieldName;
  }
}
