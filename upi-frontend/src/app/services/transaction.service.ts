import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Transaction, TransferRequest, TransactionStatus, TransactionFilter } from '../models/transaction.model';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {

  constructor(private apiService: ApiService) {}

  // Process money transfer
  processTransfer(transferData: TransferRequest): Observable<Transaction> {
    return this.apiService.post<Transaction>('/api/transactions/transfer', transferData);
  }

  // Get transaction by ID
  getTransactionById(id: number): Observable<Transaction> {
    return this.apiService.get<Transaction>(`/api/transactions/${id}`);
  }

  // Get user transaction history
  getUserTransactionHistory(upiId: string): Observable<Transaction[]> {
    return this.apiService.get<Transaction[]>(`/api/transactions/user/${upiId}`);
  }

  // Get sent transactions
  getSentTransactions(upiId: string): Observable<Transaction[]> {
    return this.apiService.get<Transaction[]>(`/api/transactions/user/${upiId}/sent`);
  }

  // Get received transactions
  getReceivedTransactions(upiId: string): Observable<Transaction[]> {
    return this.apiService.get<Transaction[]>(`/api/transactions/user/${upiId}/received`);
  }

  // Get transactions by status
  getTransactionsByStatus(status: TransactionStatus): Observable<Transaction[]> {
    return this.apiService.get<Transaction[]>(`/api/transactions/status/${status}`);
  }

  // Get filtered transactions
  getFilteredTransactions(upiId: string, filter: TransactionFilter): Observable<Transaction[]> {
    const params: any = {};
    
    if (filter.startDate) params.startDate = filter.startDate;
    if (filter.endDate) params.endDate = filter.endDate;
    if (filter.status) params.status = filter.status;
    if (filter.limit) params.limit = filter.limit;

    return this.apiService.get<Transaction[]>(`/api/transactions/user/${upiId}/filter`, params);
  }

  // Get recent transactions
  getRecentTransactions(upiId: string, limit: number = 10): Observable<Transaction[]> {
    return this.apiService.get<Transaction[]>(`/api/transactions/user/${upiId}/recent`, { limit });
  }

  // Get transaction count
  getTransactionCount(upiId: string): Observable<number> {
    return this.apiService.get<number>(`/api/transactions/user/${upiId}/count`);
  }

  // Get transaction by reference
  getTransactionByReference(transactionRef: string): Observable<Transaction> {
    return this.apiService.get<Transaction>(`/api/transactions/reference/${transactionRef}`);
  }

  // Helper method to format transaction amount for display
  formatTransactionAmount(transaction: Transaction, currentUserUpiId: string): { amount: number, type: 'sent' | 'received' } {
    const isSent = transaction.senderUpiId === currentUserUpiId;
    return {
      amount: transaction.amount,
      type: isSent ? 'sent' : 'received'
    };
  }

  // Helper method to get transaction counterparty
  getTransactionCounterparty(transaction: Transaction, currentUserUpiId: string): string {
    return transaction.senderUpiId === currentUserUpiId 
      ? transaction.receiverUpiId 
      : transaction.senderUpiId;
  }
}