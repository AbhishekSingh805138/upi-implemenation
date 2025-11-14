import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import {
  PaymentCategory,
  RechargePlan,
  MobileRechargeRequest,
  DTHRechargeRequest,
  ElectricityBillPaymentRequest,
  CreditCardPaymentRequest,
  InsurancePremiumRequest,
  UtilityPaymentResponse,
  BillDetails,
  PaymentHistory,
  PaymentReceipt
} from '../models/utility.model';

@Injectable({
  providedIn: 'root'
})
export class UtilityService {
  private apiUrl = 'http://localhost:8080/api/utilities';

  constructor(private http: HttpClient) {}

  // ========== Payment Categories ==========
  getPaymentCategories(): Observable<PaymentCategory[]> {
    return this.http.get<PaymentCategory[]>(`${this.apiUrl}/categories`)
      .pipe(
        retry(2),
        catchError(this.handleError)
      );
  }

  // ========== Mobile Recharge ==========
  getMobileOperators(): Observable<PaymentCategory[]> {
    return this.http.get<PaymentCategory[]>(`${this.apiUrl}/recharge/mobile/operators`)
      .pipe(
        retry(2),
        catchError(this.handleError)
      );
  }

  getRechargePlans(operatorCode: string): Observable<RechargePlan[]> {
    return this.http.get<RechargePlan[]>(`${this.apiUrl}/recharge/mobile/plans/${operatorCode}`)
      .pipe(
        retry(2),
        catchError(this.handleError)
      );
  }

  processMobileRecharge(request: MobileRechargeRequest): Observable<UtilityPaymentResponse> {
    return this.http.post<UtilityPaymentResponse>(`${this.apiUrl}/recharge/mobile`, request)
      .pipe(
        catchError(this.handleError)
      );
  }

  // ========== DTH Recharge ==========
  getDTHOperators(): Observable<PaymentCategory[]> {
    return this.http.get<PaymentCategory[]>(`${this.apiUrl}/recharge/dth/operators`)
      .pipe(
        retry(2),
        catchError(this.handleError)
      );
  }

  getDTHPlans(operatorCode: string, subscriberId?: string): Observable<RechargePlan[]> {
    const url = subscriberId 
      ? `${this.apiUrl}/recharge/dth/plans/${operatorCode}/${subscriberId}`
      : `${this.apiUrl}/recharge/dth/plans/${operatorCode}/default`;
    return this.http.get<RechargePlan[]>(url)
      .pipe(
        retry(2),
        catchError(this.handleError)
      );
  }

  processDTHRecharge(request: DTHRechargeRequest): Observable<UtilityPaymentResponse> {
    return this.http.post<UtilityPaymentResponse>(`${this.apiUrl}/recharge/dth`, request)
      .pipe(
        catchError(this.handleError)
      );
  }

  // ========== Electricity Bill ==========
  getElectricityProviders(): Observable<PaymentCategory[]> {
    return this.http.get<PaymentCategory[]>(`${this.apiUrl}/bills/electricity/providers`)
      .pipe(
        retry(2),
        catchError(this.handleError)
      );
  }

  fetchElectricityBill(providerCode: string, consumerNumber: string): Observable<BillDetails> {
    const params = new HttpParams()
      .set('providerCode', providerCode)
      .set('consumerNumber', consumerNumber);
    return this.http.get<BillDetails>(`${this.apiUrl}/bills/electricity/fetch`, { params })
      .pipe(
        retry(2),
        catchError(this.handleError)
      );
  }

  payElectricityBill(request: ElectricityBillPaymentRequest): Observable<UtilityPaymentResponse> {
    return this.http.post<UtilityPaymentResponse>(`${this.apiUrl}/bills/electricity`, request)
      .pipe(
        catchError(this.handleError)
      );
  }

  // ========== Credit Card Bill ==========
  getCreditCardIssuers(): Observable<PaymentCategory[]> {
    return this.http.get<PaymentCategory[]>(`${this.apiUrl}/bills/credit-card/issuers`)
      .pipe(
        retry(2),
        catchError(this.handleError)
      );
  }

  payCreditCardBill(request: CreditCardPaymentRequest): Observable<UtilityPaymentResponse> {
    return this.http.post<UtilityPaymentResponse>(`${this.apiUrl}/bills/credit-card`, request)
      .pipe(
        catchError(this.handleError)
      );
  }

  // ========== Insurance Premium ==========
  payInsurancePremium(request: InsurancePremiumRequest): Observable<UtilityPaymentResponse> {
    return this.http.post<UtilityPaymentResponse>(`${this.apiUrl}/bills/insurance`, request)
      .pipe(
        catchError(this.handleError)
      );
  }

  // ========== Payment History ==========
  getPaymentHistory(userId: number): Observable<PaymentHistory[]> {
    return this.http.get<PaymentHistory[]>(`${this.apiUrl}/payments/${userId}`)
      .pipe(
        retry(2),
        catchError(this.handleError)
      );
  }

  getPaymentsByCategory(userId: number, categoryName: string): Observable<PaymentHistory[]> {
    return this.http.get<PaymentHistory[]>(`${this.apiUrl}/payments/${userId}/${categoryName}`)
      .pipe(
        retry(2),
        catchError(this.handleError)
      );
  }

  getPaymentsByDateRange(userId: number, startDate: string, endDate: string): Observable<PaymentHistory[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<PaymentHistory[]>(`${this.apiUrl}/payments/${userId}/daterange`, { params })
      .pipe(
        retry(2),
        catchError(this.handleError)
      );
  }

  getPaymentDetails(transactionId: number): Observable<PaymentHistory> {
    return this.http.get<PaymentHistory>(`${this.apiUrl}/payments/transaction/${transactionId}`)
      .pipe(
        retry(2),
        catchError(this.handleError)
      );
  }

  generateReceipt(transactionId: number): Observable<PaymentReceipt> {
    return this.http.post<PaymentReceipt>(`${this.apiUrl}/payments/${transactionId}/receipt`, {})
      .pipe(
        retry(2),
        catchError(this.handleError)
      );
  }

  // ========== Error Handling ==========
  private handleError(error: any): Observable<never> {
    let errorMessage = 'An error occurred';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = error.error?.message || `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
