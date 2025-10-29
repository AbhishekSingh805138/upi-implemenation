import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { UserService } from './services/user.service';
import { AccountService } from './services/account.service';
import { User } from './models/user.model';
import { Account } from './models/account.model';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet, 
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatSidenavModule,
    MatListModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'UPI Payment System';
  currentUser: User | null = null;
  currentAccount: Account | null = null;
  showNavigation = false;

  constructor(
    private userService: UserService,
    private accountService: AccountService,
    private router: Router
  ) {}

  ngOnInit() {
    // Subscribe to user changes
    this.userService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.updateNavigationVisibility();
    });

    // Subscribe to account changes
    this.accountService.currentAccount$.subscribe(account => {
      this.currentAccount = account;
    });

    // Hide navigation on login/register pages
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.updateNavigationVisibility();
    });
  }

  private updateNavigationVisibility() {
    const currentUrl = this.router.url;
    const authPages = ['/login', '/register'];
    this.showNavigation = this.currentUser !== null && !authPages.includes(currentUrl);
  }

  logout() {
    this.userService.logout();
    this.accountService.clearCurrentAccount();
    this.router.navigate(['/login']);
  }

  navigateTo(route: string) {
    this.router.navigate([route]);
  }
}
