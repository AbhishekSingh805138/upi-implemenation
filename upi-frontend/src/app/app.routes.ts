import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { TransferComponent } from './components/transfer/transfer.component';
import { TransactionHistoryComponent } from './components/transaction-history/transaction-history.component';
import { ProfileComponent } from './components/profile/profile.component';
import { AccountSetupComponent } from './components/account-setup/account-setup.component';
import { UtilityServicesComponent } from './components/utility-services/utility-services.component';
import { MobileRechargeComponent } from './components/mobile-recharge/mobile-recharge.component';
import { BillPaymentComponent } from './components/bill-payment/bill-payment.component';
import { PaymentHistoryComponent } from './components/payment-history/payment-history.component';
import { SavedBillersComponent } from './components/saved-billers/saved-billers.component';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'account-setup', component: AccountSetupComponent, canActivate: [AuthGuard] },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'transfer', component: TransferComponent, canActivate: [AuthGuard] },
  { path: 'history', component: TransactionHistoryComponent, canActivate: [AuthGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  
  // Utility Services Routes
  { path: 'utilities', component: UtilityServicesComponent, canActivate: [AuthGuard] },
  { path: 'utilities/mobile-recharge', component: MobileRechargeComponent, canActivate: [AuthGuard] },
  { path: 'utilities/dth-recharge', component: MobileRechargeComponent, canActivate: [AuthGuard] },
  { path: 'utilities/bills/electricity', component: BillPaymentComponent, canActivate: [AuthGuard] },
  { path: 'utilities/bills/credit-card', component: BillPaymentComponent, canActivate: [AuthGuard] },
  { path: 'utilities/bills/insurance', component: BillPaymentComponent, canActivate: [AuthGuard] },
  { path: 'utilities/saved-billers', component: SavedBillersComponent, canActivate: [AuthGuard] },
  { path: 'utilities/history', component: PaymentHistoryComponent, canActivate: [AuthGuard] },
  
  { path: '**', redirectTo: '/login' }
];
