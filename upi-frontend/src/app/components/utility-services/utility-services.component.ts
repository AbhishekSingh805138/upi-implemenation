import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { UtilityService } from '../../services/utility.service';
import { PaymentCategory } from '../../models/utility.model';

@Component({
  selector: 'app-utility-services',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './utility-services.component.html',
  styleUrls: ['./utility-services.component.scss']
})
export class UtilityServicesComponent implements OnInit {
  categories: PaymentCategory[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private utilityService: UtilityService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.error = null;
    this.utilityService.getPaymentCategories().subscribe({
      next: (categories) => {
        this.categories = categories.filter(c => c.isActive);
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load payment categories';
        this.loading = false;
        console.error('Error loading categories:', error);
      }
    });
  }

  navigateToService(category: PaymentCategory): void {
    const routeMap: { [key: string]: string } = {
      'MOBILE_RECHARGE': '/utilities/mobile-recharge',
      'DTH_RECHARGE': '/utilities/dth-recharge',
      'ELECTRICITY_BILL': '/utilities/bills/electricity',
      'CREDIT_CARD_BILL': '/utilities/bills/credit-card',
      'INSURANCE_PREMIUM': '/utilities/bills/insurance'
    };

    const route = routeMap[category.name];
    if (route) {
      console.log('Navigating to:', route, 'for category:', category.name);
      this.router.navigate([route]);
    } else {
      console.error('No route found for category:', category.name);
    }
  }

  getIconClass(categoryName: string): string {
    const iconMap: { [key: string]: string } = {
      'MOBILE_RECHARGE': 'bi-phone',
      'DTH_RECHARGE': 'bi-tv',
      'ELECTRICITY_BILL': 'bi-lightning-charge',
      'CREDIT_CARD_BILL': 'bi-credit-card',
      'INSURANCE_PREMIUM': 'bi-shield-check'
    };
    return iconMap[categoryName] || 'bi-wallet2';
  }
}
