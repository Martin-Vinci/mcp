import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AgentCommissionReportComponent } from './agent-commission-report/agent-commission-report.component';
import { AgentListingReportComponent } from './agent-listing-report/agent-listing-report.component';
import { CustomerListingReportComponent } from './customer-listing-report/customer-listing-report.component';
import { FloatAccountReportComponent } from './float-account-report/float-account-report.component';
import { TransactionBandReportComponent } from './transaction-band-report/transaction-band-report.component';
import { TransactionListingReportComponent } from './transaction-listing-report/transaction-listing-report.component';
import { UserListingReportComponent } from './user-listing-report/user-listing-report.component';
import { VoucherListingReportComponent } from './voucher-listing-report/voucher-listing-report.component';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'agent-commission-report', component: AgentCommissionReportComponent,
        data: { title: "Agent Commission Report", breadcrumb: "Agent Commission Report" }
      },
      {
        path: 'agent-listing-report', component: AgentListingReportComponent,
        data: { title: "Transaction Details", breadcrumb: "Transaction Details" }
      },
      {
        path: 'customer-listing-report', component: CustomerListingReportComponent,
        data: { title: "Customer Listing", breadcrumb: "Voucher Requests" }
      },  
      {
        path: 'transaction-band-report', component: TransactionBandReportComponent,
        data: { title: "Transaction Band", breadcrumb: "Transaction Band" }
      },  
      {
        path: 'voucher-listing-report', component: VoucherListingReportComponent,
        data: { title: "Voucher Transactions", breadcrumb: "Voucher Transactions" }
      },  
      {
        path: 'transaction-listing-report', component: TransactionListingReportComponent,
        data: { title: "Transaction Listing", breadcrumb: "Transaction Listing" }
      }, 
      {
        path: 'user-listing-report', component: UserListingReportComponent,
        data: { title: "User Listing", breadcrumb: "User Listing" }
      },   
   ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReportsRoutingModule { }
