import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { UtilityService } from '../../services/utility.service';
import { AccountService } from '../../services/account.service';
import {
  ElectricityBillPaymentRequest,
  CreditCardPaymentRequest,
  InsurancePremiumRequest,
  BillDetails,
  UtilityPaymentResponse,
  PaymentCategory
} from '../../models/utility.model';

@Component({
  selector: 'app-bill-payment',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './bill-payment.component.html',
  styleUrls: ['./bill-payment.component.scss']
})
export class BillPaymentComponent implements OnInit {
  billType: 'electricity' | 'credit-card' | 'insurance' = 'electricity';
  
  // Common fields
  upiId = '';
  amount: number | null = null;
  
  // Electricity fields
  providerCode = '';
  consumerNumber = '';
  billDetails: BillDetails | null = null;
  
  // Credit card fields
  issuerCode = '';
  cardLast4Digits = '';
  
  // Insurance fields
  policyNumber = '';
  
  providers: PaymentCategory[] = [];
  loading = false;
  fetchingBill = false;
  processing = false;
  error: string | null = null;
  success: UtilityPaymentResponse | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private utilityService: UtilityService,
    private accountService: AccountService
  ) {}

  ngOnInit(): void {
    this.route.url.subscribe(segments => {
      const path = segments[segments.length - 1]?.path;
      if (path === 'electricity') {
        this.billType = 'electricity';
      } else if (path === 'credit-card') {
        this.billType = 'credit-card';
      } else if (path === 'insurance') {
        this.billType = 'insurance';
      }
      this.loadProviders();
    });
    this.loadUserAccount();
  }

  loadProviders(): void {
    this.loading = true;
    if (this.billType === 'credit-card') {
      this.utilityService.getCreditCardIssuers().subscribe({
        next: (providers) => {
          this.providers = providers.filter(p => p.isActive);
          this.loading = false;
        },
        error: () => {
          this.error = 'Failed to load providers';
          this.loading = false;
        }
      });
    } else {
      this.loading = false;
    }
  }

  loadUserAccount(): void {
    const currentUserStr = localStorage.getItem('currentUser');
    if (!currentUserStr) {
      this.error = 'User not logged in';
      return;
    }

    const currentUser = JSON.parse(currentUserStr);
    const userId = currentUser.id;

    if (userId) {
      this.accountService.getAccountByUserId(userId).subscribe({
        next: (account) => {
          this.upiId = account.upiId;
        },
        error: (error) => {
          console.error('Error loading account:', error);
          this.error = 'Failed to load UPI ID';
        }
      });
    }
  }

  fetchBill(): void {
    if (this.billType !== 'electricity') return;
    
    if (!this.providerCode || !this.consumerNumber) {
      this.error = 'Please enter provider code and consumer number';
      return;
    }

    this.fetchingBill = true;
    this.error = null;
    
    this.utilityService.fetchElectricityBill(this.providerCode, this.consumerNumber).subscribe({
      next: (bill) => {
        this.billDetails = bill;
        this.amount = bill.amountDue;
        this.fetchingBill = false;
      },
      error: (error) => {
        this.error = error.message || 'Failed to fetch bill details';
        this.fetchingBill = false;
      }
    });
  }

  processBillPayment(): void {
    if (!this.validateForm()) {
      return;
    }

    this.processing = true;
    this.error = null;
    this.success = null;

    if (this.billType === 'electricity') {
      this.payElectricityBill();
    } else if (this.billType === 'credit-card') {
      this.payCreditCardBill();
    } else if (this.billType === 'insurance') {
      this.payInsurancePremium();
    }
  }

  payElectricityBill(): void {
    const request: ElectricityBillPaymentRequest = {
      upiId: this.upiId,
      providerCode: this.providerCode,
      consumerNumber: this.consumerNumber,
      amount: this.amount!
    };

    this.utilityService.payElectricityBill(request).subscribe({
      next: (response) => {
        this.success = response;
        this.processing = false;
        this.resetForm();
      },
      error: (error) => {
        this.error = error.message || 'Failed to process payment';
        this.processing = false;
      }
    });
  }

  payCreditCardBill(): void {
    const request: CreditCardPaymentRequest = {
      upiId: this.upiId,
      issuerCode: this.issuerCode,
      cardLast4Digits: this.cardLast4Digits,
      amount: this.amount!
    };

    this.utilityService.payCreditCardBill(request).subscribe({
      next: (response) => {
        this.success = response;
        this.processing = false;
        this.resetForm();
      },
      error: (error) => {
        this.error = error.message || 'Failed to process payment';
        this.processing = false;
      }
    });
  }

  payInsurancePremium(): void {
    const request: InsurancePremiumRequest = {
      upiId: this.upiId,
      providerCode: this.providerCode,
      policyNumber: this.policyNumber,
      amount: this.amount!
    };

    this.utilityService.payInsurancePremium(request).subscribe({
      next: (response) => {
        this.success = response;
        this.processing = false;
        this.resetForm();
      },
      error: (error) => {
        this.error = error.message || 'Failed to process payment';
        this.processing = false;
      }
    });
  }

  validateForm(): boolean {
    if (!this.amount || this.amount <= 0) {
      this.error = 'Please enter a valid amount';
      return false;
    }

    if (this.billType === 'electricity') {
      if (!this.providerCode || !this.consumerNumber) {
        this.error = 'Please enter all required fields';
        return false;
      }
    } else if (this.billType === 'credit-card') {
      if (!this.issuerCode || !this.cardLast4Digits || this.cardLast4Digits.length !== 4) {
        this.error = 'Please enter valid card details';
        return false;
      }
    } else if (this.billType === 'insurance') {
      if (!this.providerCode || !this.policyNumber) {
        this.error = 'Please enter all required fields';
        return false;
      }
    }

    return true;
  }

  resetForm(): void {
    this.amount = null;
    this.providerCode = '';
    this.consumerNumber = '';
    this.issuerCode = '';
    this.cardLast4Digits = '';
    this.policyNumber = '';
    this.billDetails = null;
  }

  getTitle(): string {
    const titles = {
      'electricity': 'Electricity Bill Payment',
      'credit-card': 'Credit Card Bill Payment',
      'insurance': 'Insurance Premium Payment'
    };
    return titles[this.billType];
  }

  getIcon(): string {
    const icons = {
      'electricity': 'bi-lightning-charge',
      'credit-card': 'bi-credit-card',
      'insurance': 'bi-shield-check'
    };
    return icons[this.billType];
  }

  getSubtitle(): string {
    const subtitles = {
      'electricity': 'Pay your electricity bills instantly and securely',
      'credit-card': 'Clear your credit card dues with ease',
      'insurance': 'Keep your insurance coverage active with timely premium payments'
    };
    return subtitles[this.billType];
  }
}
