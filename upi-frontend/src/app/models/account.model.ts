export interface Account {
  id: number;
  userId: number;
  upiId: string;
  accountNumber: string;
  balance: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAccountRequest {
  userId: number;
  initialBalance: number;
}

export interface BalanceResponse {
  balance: number;
  upiId: string;
}

export interface BalanceUpdateRequest {
  amount: number;
  operation: 'CREDIT' | 'DEBIT';
}