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
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatStepperModule } from '@angular/material/stepper';
import { UserService } from '../../services/user.service';
import { AccountService } from '../../services/account.service';
import { TransactionService } from '../../services/transaction.service';
import { User } from '../../models/user.model';
import { Account } from '../../models/account.model';
import { Transaction } from '../../models/transaction.model';

@Component({
  selector: 'app-transfer',
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
    MatDialogModule,
    MatStepperModule
  ],
  templateUrl: './transfer.component.html',
  styleUrl: './transfer.component.scss'
})
export class TransferComponent implements OnInit {
  currentUser: User | null = null;
  currentAccount: Account | null = null;
  transferForm: FormGroup;
  confirmationForm: FormGroup;
  isLoading = false;
  isValidatingUpi = false;
  upiValidationResult: boolean | null = null;
  completedTransaction: Transaction | null = null;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private accountService: AccountService,
    private transactionService: TransactionService,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.transferForm = this.fb.group({
      receiverUpiId: ['', [
        Validators.required,
        Validators.pattern(/^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$/)
      ]],
      amount: ['', [
        Validators.required,
        Validators.min(1),
        Validators.max(100000)
      ]],
      description: ['', [Validators.maxLength(255)]]
    });

    this.confirmationForm = this.fb.group({
      confirmAmount: ['', [Validators.required]]
    });
  }

  ngOnInit() {
    this.currentUser = this.userService.getCurrentUser();
    this.currentAccount = this.accountService.getCurrentAccount();

    if (!this.currentUser || !this.currentAccount) {
      this.router.navigate(['/login']);
      return;
    }

    // Refresh account balance
    this.accountService.refreshCurrentAccountBalance().subscribe();
  }

  validateUpiId() {
    const upiId = this.transferForm.get('receiverUpiId')?.value;
    
    if (!upiId || this.transferForm.get('receiverUpiId')?.invalid) {
      return;
    }

    // Check if it's the same as sender's UPI ID
    if (upiId === this.currentAccount?.upiId) {
      this.upiValidationResult = false;
      this.transferForm.get('receiverUpiId')?.setErrors({ 'sameAccount': true });
      return;
    }

    this.isValidatingUpi = true;
    this.upiValidationResult = null;

    this.accountService.validateUpiId(upiId).subscribe({
      next: (isValid) => {
        this.upiValidationResult = isValid;
        this.isValidatingUpi = false;
        
        if (!isValid) {
          this.transferForm.get('receiverUpiId')?.setErrors({ 'invalidUpi': true });
        } else {
          // Clear any existing errors for this field
          const control = this.transferForm.get('receiverUpiId');
          if (control?.errors) {
            delete control.errors['invalidUpi'];
            if (Object.keys(control.errors).length === 0) {
              control.setErrors(null);
            }
          }
        }
      },
      error: (error) => {
        this.upiValidationResult = false;
        this.isValidatingUpi = false;
        this.transferForm.get('receiverUpiId')?.setErrors({ 'validationError': true });
      }
    });
  }

  onSubmit() {
    if (this.transferForm.valid && this.upiValidationResult === true) {
      this.isLoading = true;
      
      const transferData = {
        senderUpiId: this.currentAccount!.upiId,
        receiverUpiId: this.transferForm.value.receiverUpiId,
        amount: this.transferForm.value.amount,
        description: this.transferForm.value.description || 'Money transfer'
      };

      this.transactionService.processTransfer(transferData).subscribe({
        next: (transaction) => {
          this.completedTransaction = transaction;
          this.isLoading = false;
          
          // Refresh account balance
          this.accountService.refreshCurrentAccountBalance().subscribe();
          
          this.snackBar.open(
            `Transfer successful! Transaction ID: ${transaction.transactionRef}`, 
            'Close', 
            { duration: 5000 }
          );
          
          // Reset form
          this.transferForm.reset();
          this.upiValidationResult = null;
        },
        error: (error) => {
          this.isLoading = false;
          this.snackBar.open(
            error.message || 'Transfer failed. Please try again.', 
            'Close', 
            { duration: 5000 }
          );
        }
      });
    }
  }

  getErrorMessage(fieldName: string): string {
    const field = this.transferForm.get(fieldName);
    
    if (field?.hasError('required')) {
      return `${this.getFieldDisplayName(fieldName)} is required`;
    }
    
    if (field?.hasError('pattern')) {
      return 'Please enter a valid UPI ID (e.g., user@bank)';
    }
    
    if (field?.hasError('min')) {
      return 'Amount must be at least ₹1';
    }
    
    if (field?.hasError('max')) {
      return 'Amount cannot exceed ₹1,00,000';
    }
    
    if (field?.hasError('maxlength')) {
      return 'Description cannot exceed 255 characters';
    }
    
    if (field?.hasError('sameAccount')) {
      return 'Cannot transfer to your own account';
    }
    
    if (field?.hasError('invalidUpi')) {
      return 'UPI ID not found or invalid';
    }
    
    if (field?.hasError('validationError')) {
      return 'Error validating UPI ID. Please try again.';
    }
    
    return '';
  }

  private getFieldDisplayName(fieldName: string): string {
    const displayNames: { [key: string]: string } = {
      receiverUpiId: 'Receiver UPI ID',
      amount: 'Amount',
      description: 'Description'
    };
    return displayNames[fieldName] || fieldName;
  }

  canTransfer(): boolean {
    return this.transferForm.valid && 
           this.upiValidationResult === true && 
           !this.isLoading && 
           !this.isValidatingUpi;
  }

  goToDashboard() {
    this.router.navigate(['/dashboard']);
  }

  viewTransactionHistory() {
    this.router.navigate(['/history']);
  }

  makeAnotherTransfer() {
    this.completedTransaction = null;
    this.transferForm.reset();
    this.upiValidationResult = null;
  }
}
