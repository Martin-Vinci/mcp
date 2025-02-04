import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CreditApplicApprovalComponent } from './credit-applic-approval/credit-applic-approval.component';
import { CreditApplicMaintainComponent } from './credit-applic-maintain/credit-applic-maintain.component';
import { CreditApplicSearchComponent } from './credit-applic-search/credit-applic-search.component';
import { CustomerMaintainComponent } from './customer-maintain/customer-maintain.component';
import { CustomerSearchComponent } from './customer-search/customer-search.component';
import { LoanAccountMaintainComponent } from './loan-account-maintain/loan-account-maintain.component';
import { LoanAccountSearchComponent } from './loan-account-search/loan-account-search.component';
import { LoanRepaymentDetailsComponent } from './loan-repayment-details/loan-repayment-details.component';
import { LoanRepaymentComponent } from './loan-repayment/loan-repayment.component';

const routes: Routes = [
  {
    path: 'customer-search', component: CustomerSearchComponent,
    data: { title: "Customer Search", breadcrumb: "Customer Search" }
  },
  {
    path: 'customer/:id', component: CustomerMaintainComponent,
    data: { title: "Customer Maintain", breadcrumb: "Customer Maintain" }
  },
  {
    path: 'credit-appl-search', component: CreditApplicSearchComponent,
    data: { title: "Credit Application", breadcrumb: "Credit Application" }
  },
  {
    path: 'credit-appl/:id', component: CreditApplicMaintainComponent,
    data: { title: "Credit Maintain", breadcrumb: "Credit Maintain" }
  },
  {
    path: 'loan-account-search', component: LoanAccountSearchComponent,
    data: { title: "Agent Outlet", breadcrumb: "Agent Outlet" }
  },
  {
    path: 'loan-account/:id', component: LoanAccountMaintainComponent,
    data: { title: "Agent Outlet", breadcrumb: "Maintain Loan Account" }
  },
  {
    path: 'loan-repayment-search', component: LoanRepaymentComponent,
    data: { title: "Agent Outlet", breadcrumb: "Repayment History" }
  },
  {
    path: 'loan-repayment/:id', component: LoanRepaymentDetailsComponent,
    data: { title: "Agent Outlet", breadcrumb: "Repayment Details" }
  },
  {
    path: 'credit-applic-approval', component: CreditApplicApprovalComponent,
    data: { title: "Agent Outlet", breadcrumb: "Credit Approval" }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class WebcashRoutingModule { }
