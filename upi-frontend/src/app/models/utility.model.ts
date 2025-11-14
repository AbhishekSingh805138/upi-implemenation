export interface PaymentCategory {
  id: number;
  name: string;
  displayName: string;
  iconUrl: string;
  isActive: boolean;
}

export interface RechargePlan {
  id: number;
  providerId: number;
  planCode: string;
  planName: string;
  amount: number;
  validityDays: number;
  description: string;
  isActive: boolean;
}

export interface MobileRechargeRequest {
  upiId: string;
  mobileNumber: string;
  operatorCode: string;
  amount: number;
  planCode?: string;
}

export interface DTHRechargeRequest {
  upiId: string;
  subscriberId: string;
  operatorCode: string;
  amount: number;
  planCode?: string;
}

export interface ElectricityBillPaymentRequest {
  upiId: string;
  providerCode: string;
  consumerNumber: string;
  amount: number;
  billingCycle?: string;
}

export interface CreditCardPaymentRequest {
  upiId: string;
  issuerCode: string;
  cardLast4Digits: string;
  amount: number;
}

export interface InsurancePremiumRequest {
  upiId: string;
  providerCode: string;
  policyNumber: string;
  amount: number;
}

export interface UtilityPaymentResponse {
  transactionRef: string;
  providerTransactionRef: string;
  status: string;
  amount: number;
  message: string;
  timestamp: string;
  receiptDetails?: any;
}

export interface BillDetails {
  consumerNumber: string;
  consumerName: string;
  amountDue: number;
  dueDate: string;
  billingPeriod: string;
  additionalDetails?: any;
}

export interface SavedBiller {
  id?: number;
  userId: number;
  categoryId: number;
  categoryName?: string;
  providerId: number;
  providerName?: string;
  accountIdentifier: string;
  nickname: string;
  accountHolderName: string;
}

export interface PaymentHistory {
  id: number;
  userId: number;
  upiId: string;
  categoryName: string;
  providerName: string;
  accountIdentifier: string;
  amount: number;
  paymentStatus: string;
  transactionRef: string;
  providerTransactionRef: string;
  timestamp: string;
}

export interface PaymentReceipt {
  transactionRef: string;
  providerTransactionRef: string;
  categoryName: string;
  providerName: string;
  accountIdentifier: string;
  amount: number;
  status: string;
  timestamp: string;
  receiptDetails: any;
}
