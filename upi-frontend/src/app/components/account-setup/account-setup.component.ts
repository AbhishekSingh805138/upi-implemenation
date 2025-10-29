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
import { UserService } from '../../services/user.service';
import { AccountService } from '../../services/account.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-account-setup',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './account-setup.component.html',
  styleUrl: './account-setup.component.scss'
})
export class AccountSetupComponent implements OnInit {
  accountForm: FormGroup;
  isLoading = false;
  currentUser: User | null = null;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private accountService: AccountService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.accountForm = this.fb.group({
      initialBalance: [1000, [
        Validators.required, 
        Validators.min(0),
        Validators.max(1000000)
      ]]
    });
  }

  ngOnInit() {
    this.currentUser = this.userService.getCurrentUser();
    
    if (!this.currentUser) {
      this.router.navigate(['/login']);
      return;
    }

    // Check if user already has an account
    this.accountService.getAccountByUserId(this.currentUser.id!).subscribe({
      next: (account) => {
        // User already has an account, redirect to dashboard
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        // User doesn't have an account, stay on this page
        console.log('User needs to create an account');
      }
    });
  }

  onSubmit() {
    if (this.accountForm.valid && this.currentUser) {
      this.isLoading = true;
      
      const accountData = {
        userId: this.currentUser.id!,
        initialBalance: this.accountForm.value.initialBalance
      };

      this.accountService.createAccount(accountData).subscribe({
        next: (account) => {
          this.snackBar.open(
            `Account created successfully! Your UPI ID: ${account.upiId}`, 
            'Close', 
            { duration: 5000 }
          );
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.snackBar.open(error.message || 'Failed to create account', 'Close', { duration: 5000 });
          this.isLoading = false;
        }
      });
    }
  }

  getErrorMessage(fieldName: string): string {
    const field = this.accountForm.get(fieldName);
    
    if (field?.hasError('required')) {
      return 'Initial balance is required';
    }
    
    if (field?.hasError('min')) {
      return 'Initial balance cannot be negative';
    }
    
    if (field?.hasError('max')) {
      return 'Initial balance cannot exceed ₹10,00,000';
    }
    
    return '';
  }
}
