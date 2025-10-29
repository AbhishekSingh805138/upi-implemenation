import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { UserService } from '../../services/user.service';
import { AccountService } from '../../services/account.service';

@Component({
  selector: 'app-login',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private accountService: AccountService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.loginForm = this.fb.group({
      identifier: ['', [Validators.required]]
    });
  }

  ngOnInit() {
    // Redirect if already logged in
    if (this.userService.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    }
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.isLoading = true;
      const loginData = this.loginForm.value;

      this.userService.login(loginData).subscribe({
        next: (user) => {
          this.snackBar.open('Login successful!', 'Close', { duration: 3000 });
          
          // Try to load user's account
          this.accountService.getAccountByUserId(user.id!).subscribe({
            next: (account) => {
              this.router.navigate(['/dashboard']);
            },
            error: (error) => {
              // User doesn't have an account, redirect to account setup
              this.router.navigate(['/account-setup']);
            }
          });
        },
        error: (error) => {
          this.snackBar.open(error.message || 'Login failed', 'Close', { duration: 5000 });
          this.isLoading = false;
        }
      });
    }
  }

  getErrorMessage(fieldName: string): string {
    const field = this.loginForm.get(fieldName);
    if (field?.hasError('required')) {
      return `${fieldName} is required`;
    }
    return '';
  }
}
