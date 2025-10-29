import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { Account, CreateAccountRequest, BalanceResponse, BalanceUpdateRequest } from '../models/account.model';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private currentAccountSubject = new BehaviorSubject<Account | null>(null);
  public currentAccount$ = this.currentAccountSubject.asObservable();

  constructor(private apiService: ApiService) { }

  // Create new account
  createAccount(accountData: CreateAccountRequest): Observable<Account> {
    return this.apiService.post<Account>('/api/accounts', accountData)
      .pipe(
        tap(account => {
          this.setCurrentAccount(account);
        })
      );
  }

  // Get account by user ID
  getAccountByUserId(userId: number): Observable<Account> {
    return this.apiService.get<Account>(`/api/accounts/${userId}`)
      .pipe(
        tap(account => {
          this.setCurrentAccount(account);
        })
      );
  }

  // Get account by UPI ID
  getAccountByUpiId(upiId: string): Observable<Account> {
    return this.apiService.get<Account>(`/api/accounts/upi/${upiId}`);
  }

  // Get balance by UPI ID
  getBalanceByUpiId(upiId: string): Observable<BalanceResponse> {
    return this.apiService.get<BalanceResponse>(`/api/accounts/upi/${upiId}/balance`);
  }

  // Get balance by account ID
  getBalanceByAccountId(accountId: number): Observable<BalanceResponse> {
    return this.apiService.get<BalanceResponse>(`/api/accounts/${accountId}/balance`);
  }

  // Update balance by UPI ID
  updateBalanceByUpiId(upiId: string, balanceData: BalanceUpdateRequest): Observable<BalanceResponse> {
    return this.apiService.put<BalanceResponse>(`/api/accounts/upi/${upiId}/balance`, balanceData);
  }

  // Update balance by account ID
  updateBalanceByAccountId(accountId: number, balanceData: BalanceUpdateRequest): Observable<BalanceResponse> {
    return this.apiService.put<BalanceResponse>(`/api/accounts/${accountId}/balance`, balanceData);
  }

  // Validate UPI ID
  validateUpiId(upiId: string): Observable<boolean> {
    return this.apiService.get<boolean>(`/api/accounts/validate/${upiId}`);
  }

  // Set current account
  setCurrentAccount(account: Account): void {
    localStorage.setItem('currentAccount', JSON.stringify(account));
    this.currentAccountSubject.next(account);
  }

  // Get current account
  getCurrentAccount(): Account | null {
    const savedAccount = localStorage.getItem('currentAccount');
    if (savedAccount && !this.currentAccountSubject.value) {
      const account = JSON.parse(savedAccount);
      this.currentAccountSubject.next(account);
      return account;
    }
    return this.currentAccountSubject.value;
  }

  // Refresh current account balance
  refreshCurrentAccountBalance(): Observable<BalanceResponse> {
    const currentAccount = this.getCurrentAccount();
    if (currentAccount) {
      return this.getBalanceByUpiId(currentAccount.upiId)
        .pipe(
          tap(balanceResponse => {
            // Update the current account with new balance
            const updatedAccount = { ...currentAccount, balance: balanceResponse.balance };
            this.setCurrentAccount(updatedAccount);
          })
        );
    }
    throw new Error('No current account found');
  }

  // Clear current account
  clearCurrentAccount(): void {
    localStorage.removeItem('currentAccount');
    this.currentAccountSubject.next(null);
  }
}