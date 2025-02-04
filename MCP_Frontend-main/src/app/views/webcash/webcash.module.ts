import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { WebcashRoutingModule } from './webcash-routing.module';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatButtonModule } from '@angular/material/button';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { ButtonModule } from 'primeng/button';
import { MenuModule } from 'primeng/menu';
import { PanelModule } from 'primeng/panel';
import { TableModule } from 'primeng/table';
import { CustomerMaintainComponent } from './customer-maintain/customer-maintain.component';
import { CustomerSearchComponent } from './customer-search/customer-search.component';
import { CreditApplicSearchComponent } from './credit-applic-search/credit-applic-search.component';
import { CreditApplicMaintainComponent } from './credit-applic-maintain/credit-applic-maintain.component';
import { CreditApplicApprovalComponent } from './credit-applic-approval/credit-applic-approval.component';
import { LoanAccountSearchComponent } from './loan-account-search/loan-account-search.component';
import { LoanAccountMaintainComponent } from './loan-account-maintain/loan-account-maintain.component';
import { ModalLoanScheduleComponent } from './modal-loan-schedule/modal-loan-schedule.component';
import { ModalLoanAccountSearchComponent } from './modal-loan-account-search/modal-loan-account-search.component';
import { LoanRepaymentComponent } from './loan-repayment/loan-repayment.component';
import { ModalCustomerSearchComponent } from './modal-customer-search/modal-customer-search.component';
import { LoanRepaymentDetailsComponent } from './loan-repayment-details/loan-repayment-details.component';
import { MatMomentDateModule } from '@angular/material-moment-adapter';


@NgModule({
  declarations: [CustomerSearchComponent, CustomerMaintainComponent, CreditApplicSearchComponent, CreditApplicMaintainComponent, CreditApplicApprovalComponent, LoanAccountSearchComponent, LoanAccountMaintainComponent, ModalLoanScheduleComponent, ModalLoanAccountSearchComponent, LoanRepaymentComponent, ModalCustomerSearchComponent, LoanRepaymentDetailsComponent, ],
  imports: [
    CommonModule,
    WebcashRoutingModule,
    MatButtonModule,
    MatRadioModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    MatCheckboxModule,
    MatTabsModule,
    MatToolbarModule,
    MatIconModule,
    MatSidenavModule,
    MatListModule,
    MatFormFieldModule,
    MatCardModule,
    MatGridListModule,
    MatDialogModule,
    MatTableModule,
    MatPaginatorModule,
    MatSelectModule,
    FlexLayoutModule,
    TableModule,
    ButtonModule,
    PanelModule,
    FormsModule,
    MatMomentDateModule,
    ReactiveFormsModule,
    MatNativeDateModule,
    MatInputModule,
    MatDatepickerModule,
    MenuModule
  ]
})
export class WebcashModule { }
