import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { UtilityService } from '../../services/utility.service';
import { AccountService } from '../../services/account.service';
import { PaymentCategory, RechargePlan, MobileRechargeRequest, UtilityPaymentResponse } from '../../models/utility.model';

@Component({
  selector: 'app-mobile-recharge',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './mobile-recharge.component.html',
  styleUrls: ['./mobile-recharge.component.scss']
})
export class MobileRechargeComponent implements OnInit {
  operators: PaymentCategory[] = [];
  plans: RechargePlan[] = [];
  
  mobileNumber = '';
  selectedOperator = '';
  selectedPlan: RechargePlan | null = null;
  customAmount: number | null = null;
  upiId = '';
  
  loading = false;
  loadingPlans = false;
  processing = false;
  error: string | null = null;
  success: UtilityPaymentResponse | null = null;
  
  // Determine if this is DTH or Mobile recharge
  isDTH = false;
  pageTitle = 'Mobile Recharge';
  pageIcon = 'bi-phone-fill';
  numberLabel = 'Mobile Number';
  numberPlaceholder = 'Enter 10-digit mobile number';

  constructor(
    private utilityService: UtilityService,
    private accountService: AccountService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Check if this is DTH recharge based on route
    this.isDTH = this.router.url.includes('dth-recharge');
    
    if (this.isDTH) {
      this.pageTitle = 'DTH Recharge';
      this.pageIcon = 'bi-tv-fill';
      this.numberLabel = 'Subscriber ID';
      this.numberPlaceholder = 'Enter subscriber ID';
    }
    
    this.loadUserAccount();
    this.loadOperators();
  }

  loadOperators(): void {
    this.loading = true;
    const operatorCall = this.isDTH 
      ? this.utilityService.getDTHOperators()
      : this.utilityService.getMobileOperators();
      
    operatorCall.subscribe({
      next: (operators) => {
        this.operators = operators.filter(o => o.isActive);
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load operators';
        this.loading = false;
      }
    });
  }

  loadUserAccount(): void {
    // Try to get current account first
    const currentAccount = this.accountService.getCurrentAccount();
    if (currentAccount && currentAccount.upiId) {
      this.upiId = currentAccount.upiId;
      console.log('UPI ID loaded from current account:', this.upiId);
      return;
    }

    // Fallback to fetching from API
    const userId = localStorage.getItem('userId');
    console.log('User ID from localStorage:', userId);
    
    if (userId) {
      this.accountService.getAccountByUserId(parseInt(userId)).subscribe({
        next: (account) => {
          this.upiId = account.upiId;
          console.log('UPI ID loaded from API:', this.upiId);
        },
        error: (error) => {
          console.error('Error loading account:', error);
          this.error = 'Failed to load UPI ID. Please refresh the page.';
        }
      });
    } else {
      console.error('No userId found in localStorage');
      this.error = 'User not logged in. Please login again.';
    }
  }

  onOperatorChange(): void {
    if (this.selectedOperator) {
      this.loadPlans();
    }
  }

  loadPlans(): void {
    this.loadingPlans = true;
    this.plans = [];
    this.selectedPlan = null;
    
    this.utilityService.getRechargePlans(this.selectedOperator).subscribe({
      next: (plans) => {
        this.plans = plans.filter(p => p.isActive);
        this.loadingPlans = false;
      },
      error: (error) => {
        this.error = 'Failed to load recharge plans';
        this.loadingPlans = false;
      }
    });
  }

  selectPlan(plan: RechargePlan): void {
    this.selectedPlan = plan;
    this.customAmount = null;
  }

  processRecharge(): void {
    if (!this.validateForm()) {
      return;
    }

    this.processing = true;
    this.error = null;
    this.success = null;

    const request: any = {
      upiId: this.upiId,
      operatorCode: this.selectedOperator,
      amount: this.selectedPlan ? this.selectedPlan.amount : this.customAmount!,
      planCode: this.selectedPlan?.planCode
    };

    if (this.isDTH) {
      request.subscriberId = this.mobileNumber;
    } else {
      request.mobileNumber = this.mobileNumber;
    }

    const rechargeCall = this.isDTH
      ? this.utilityService.processDTHRecharge(request)
      : this.utilityService.processMobileRecharge(request);

    rechargeCall.subscribe({
      next: (response) => {
        this.success = response;
        this.processing = false;
        this.resetForm();
      },
      error: (error) => {
        this.error = error.message || 'Failed to process recharge';
        this.processing = false;
      }
    });
  }

  validateForm(): boolean {
    if (!this.mobileNumber) {
      this.error = this.isDTH ? 'Please enter subscriber ID' : 'Please enter a valid 10-digit mobile number';
      return false;
    }
    if (!this.isDTH && this.mobileNumber.length !== 10) {
      this.error = 'Please enter a valid 10-digit mobile number';
      return false;
    }
    if (!this.selectedOperator) {
      this.error = 'Please select an operator';
      return false;
    }
    if (!this.selectedPlan && !this.customAmount) {
      this.error = 'Please select a plan or enter an amount';
      return false;
    }
    if (this.customAmount && this.customAmount < 10) {
      this.error = 'Minimum recharge amount is ₹10';
      return false;
    }
    return true;
  }

  resetForm(): void {
    this.mobileNumber = '';
    this.selectedOperator = '';
    this.selectedPlan = null;
    this.customAmount = null;
    this.plans = [];
  }

  viewReceipt(): void {
    if (this.success) {
      this.router.navigate(['/utilities/history']);
    }
  }
}
