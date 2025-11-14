import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SavedBillerService } from '../../services/saved-biller.service';
import { SavedBiller } from '../../models/utility.model';

@Component({
  selector: 'app-saved-billers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './saved-billers.component.html',
  styleUrls: ['./saved-billers.component.scss']
})
export class SavedBillersComponent implements OnInit {
  billers: SavedBiller[] = [];
  filteredBillers: SavedBiller[] = [];
  selectedCategory = '';
  loading = false;
  error: string | null = null;
  editingBiller: SavedBiller | null = null;

  constructor(
    private savedBillerService: SavedBillerService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadSavedBillers();
  }

  loadSavedBillers(): void {
    const userId = localStorage.getItem('userId');
    if (!userId) {
      this.error = 'User not logged in';
      return;
    }

    this.loading = true;
    this.error = null;

    this.savedBillerService.getSavedBillers(parseInt(userId)).subscribe({
      next: (billers) => {
        this.billers = billers;
        this.filteredBillers = billers;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load saved billers';
        this.loading = false;
      }
    });
  }

  filterByCategory(): void {
    if (!this.selectedCategory) {
      this.filteredBillers = this.billers;
      return;
    }

    this.filteredBillers = this.billers.filter(
      b => b.categoryName === this.selectedCategory
    );
  }

  quickPay(biller: SavedBiller): void {
    // Navigate to appropriate payment page with pre-filled data
    const routeMap: { [key: string]: string } = {
      'MOBILE_RECHARGE': '/utilities/mobile-recharge',
      'DTH_RECHARGE': '/utilities/dth-recharge',
      'ELECTRICITY': '/utilities/bills/electricity',
      'CREDIT_CARD': '/utilities/bills/credit-card',
      'INSURANCE': '/utilities/bills/insurance'
    };

    const route = routeMap[biller.categoryName || ''];
    if (route) {
      this.router.navigate([route], {
        state: { biller }
      });
    }
  }

  editBiller(biller: SavedBiller): void {
    this.editingBiller = { ...biller };
  }

  saveBiller(): void {
    if (!this.editingBiller || !this.editingBiller.id) return;

    this.savedBillerService.updateBiller(this.editingBiller.id, this.editingBiller).subscribe({
      next: () => {
        this.editingBiller = null;
        this.loadSavedBillers();
      },
      error: (error) => {
        this.error = 'Failed to update biller';
      }
    });
  }

  deleteBiller(biller: SavedBiller): void {
    if (!biller.id) return;

    if (confirm(`Are you sure you want to delete ${biller.nickname}?`)) {
      const userId = localStorage.getItem('userId');
      if (!userId) return;

      this.savedBillerService.deleteBiller(biller.id, parseInt(userId)).subscribe({
        next: () => {
          this.loadSavedBillers();
        },
        error: (error) => {
          this.error = 'Failed to delete biller';
        }
      });
    }
  }

  cancelEdit(): void {
    this.editingBiller = null;
  }

  getUniqueCategories(): string[] {
    return [...new Set(this.billers.map(b => b.categoryName).filter((c): c is string => !!c))];
  }

  getCategoryIcon(categoryName: string): string {
    const iconMap: { [key: string]: string } = {
      'MOBILE_RECHARGE': 'bi-phone',
      'DTH_RECHARGE': 'bi-tv',
      'ELECTRICITY': 'bi-lightning-charge',
      'CREDIT_CARD': 'bi-credit-card',
      'INSURANCE': 'bi-shield-check'
    };
    return iconMap[categoryName] || 'bi-wallet2';
  }
}
