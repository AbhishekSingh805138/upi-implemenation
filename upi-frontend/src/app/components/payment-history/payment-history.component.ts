import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { UtilityService } from '../../services/utility.service';
import { PaymentHistory, PaymentReceipt } from '../../models/utility.model';

@Component({
  selector: 'app-payment-history',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './payment-history.component.html',
  styleUrls: ['./payment-history.component.scss']
})
export class PaymentHistoryComponent implements OnInit {
  payments: PaymentHistory[] = [];
  filteredPayments: PaymentHistory[] = [];
  selectedCategory = '';
  startDate = '';
  endDate = '';
  loading = false;
  error: string | null = null;
  selectedPayment: PaymentHistory | null = null;
  receipt: PaymentReceipt | null = null;

  constructor(private utilityService: UtilityService) {}

  ngOnInit(): void {
    this.loadPaymentHistory();
  }

  loadPaymentHistory(): void {
    const currentUserStr = localStorage.getItem('currentUser');
    if (!currentUserStr) {
      this.error = 'User not logged in';
      this.loading = false;
      return;
    }

    const currentUser = JSON.parse(currentUserStr);
    const userId = currentUser.id;

    if (!userId) {
      this.error = 'User ID not found';
      this.loading = false;
      return;
    }

    this.loading = true;
    this.error = null;

    this.utilityService.getPaymentHistory(userId).subscribe({
      next: (payments) => {
        this.payments = payments;
        this.filteredPayments = payments;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load payment history';
        this.loading = false;
        console.error('Error loading payment history:', error);
      }
    });
  }

  filterByCategory(): void {
    if (!this.selectedCategory) {
      this.filteredPayments = this.payments;
      return;
    }

    this.filteredPayments = this.payments.filter(
      p => p.categoryName === this.selectedCategory
    );
  }

  filterByDateRange(): void {
    if (!this.startDate || !this.endDate) {
      this.error = 'Please select both start and end dates';
      return;
    }

    const currentUserStr = localStorage.getItem('currentUser');
    if (!currentUserStr) {
      this.error = 'User not logged in';
      return;
    }

    const currentUser = JSON.parse(currentUserStr);
    const userId = currentUser.id;

    if (!userId) {
      this.error = 'User ID not found';
      return;
    }

    this.loading = true;
    this.error = null;

    // Convert dates to ISO format with time
    const startDateTime = `${this.startDate}T00:00:00`;
    const endDateTime = `${this.endDate}T23:59:59`;

    console.log('Filtering payments:', { userId, startDateTime, endDateTime });

    this.utilityService.getPaymentsByDateRange(
      userId,
      startDateTime,
      endDateTime
    ).subscribe({
      next: (payments) => {
        this.filteredPayments = payments;
        this.loading = false;
        console.log('Filtered payments:', payments);
      },
      error: (error) => {
        this.error = 'Failed to filter payments';
        this.loading = false;
        console.error('Error filtering payments:', error);
      }
    });
  }

  clearFilters(): void {
    this.selectedCategory = '';
    this.startDate = '';
    this.endDate = '';
    this.filteredPayments = this.payments;
  }

  viewDetails(payment: PaymentHistory): void {
    this.selectedPayment = payment;
  }

  downloadReceipt(transactionId: number): void {
    this.utilityService.generateReceipt(transactionId).subscribe({
      next: (receipt) => {
        this.receipt = receipt;
        // In a real app, you would generate a PDF or print the receipt
        console.log('Receipt:', receipt);
      },
      error: (error) => {
        this.error = 'Failed to generate receipt';
      }
    });
  }

  getStatusClass(status: string): string {
    const statusMap: { [key: string]: string } = {
      'COMPLETED': 'badge bg-success',
      'PENDING': 'badge bg-warning',
      'FAILED': 'badge bg-danger'
    };
    return statusMap[status] || 'badge bg-secondary';
  }

  getStatusBadgeClass(status: string): string {
    const statusMap: { [key: string]: string } = {
      'COMPLETED': 'status-badge status-success',
      'PENDING': 'status-badge status-pending',
      'FAILED': 'status-badge status-failed'
    };
    return statusMap[status] || 'status-badge status-default';
  }

  getStatusIcon(status: string): string {
    const iconMap: { [key: string]: string } = {
      'COMPLETED': 'bi bi-check-circle-fill',
      'PENDING': 'bi bi-clock-fill',
      'FAILED': 'bi bi-x-circle-fill'
    };
    return iconMap[status] || 'bi bi-question-circle-fill';
  }

  getCategoryIcon(categoryName: string): string {
    const iconMap: { [key: string]: string } = {
      'MOBILE_RECHARGE': 'bi bi-phone-fill',
      'DTH_RECHARGE': 'bi bi-tv-fill',
      'ELECTRICITY_BILL': 'bi bi-lightning-charge-fill',
      'CREDIT_CARD_BILL': 'bi bi-credit-card-fill',
      'INSURANCE_PREMIUM': 'bi bi-shield-check'
    };
    return iconMap[categoryName] || 'bi bi-wallet2';
  }

  getCategoryIconClass(categoryName: string): string {
    const classMap: { [key: string]: string } = {
      'MOBILE_RECHARGE': 'icon-mobile',
      'DTH_RECHARGE': 'icon-dth',
      'ELECTRICITY_BILL': 'icon-electricity',
      'CREDIT_CARD_BILL': 'icon-credit',
      'INSURANCE_PREMIUM': 'icon-insurance'
    };
    return classMap[categoryName] || 'icon-default';
  }

  getUniqueCategories(): string[] {
    return [...new Set(this.payments.map(p => p.categoryName))];
  }
}
