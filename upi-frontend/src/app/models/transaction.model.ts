export interface Transaction {
  id: number;
  senderUpiId: string;
  receiverUpiId: string;
  amount: number;
  description: string;
  status: TransactionStatus;
  transactionRef: string;
  createdAt: string;
}

export enum TransactionStatus {
  PENDING = 'PENDING',
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED'
}

export interface TransferRequest {
  senderUpiId: string;
  receiverUpiId: string;
  amount: number;
  description: string;
}

export interface TransactionFilter {
  startDate?: string;
  endDate?: string;
  status?: TransactionStatus;
  limit?: number;
}