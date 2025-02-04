import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ReportsRoutingModule } from './reports-routing.module';
import { AgentCommissionReportComponent } from './agent-commission-report/agent-commission-report.component';
import { FloatAccountReportComponent } from './float-account-report/float-account-report.component';
import { TransactionListingReportComponent } from './transaction-listing-report/transaction-listing-report.component';
import { UserListingReportComponent } from './user-listing-report/user-listing-report.component';
import { CustomerListingReportComponent } from './customer-listing-report/customer-listing-report.component';
import { VoucherListingReportComponent } from './voucher-listing-report/voucher-listing-report.component';
import { AgentListingReportComponent } from './agent-listing-report/agent-listing-report.component';
import { TransactionBandReportComponent } from './transaction-band-report/transaction-band-report.component';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
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
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { ButtonModule } from 'primeng/button';
import { PanelModule } from 'primeng/panel';
import { TableModule } from 'primeng/table';


@NgModule({
  declarations: [
    AgentCommissionReportComponent,
    FloatAccountReportComponent,
    TransactionListingReportComponent,
    UserListingReportComponent,
    CustomerListingReportComponent,
    VoucherListingReportComponent,
    AgentListingReportComponent,
    TransactionBandReportComponent
  ],
  imports: [
    CommonModule,
    ReportsRoutingModule,
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
    ReactiveFormsModule,
    MatNativeDateModule,
    MatInputModule,
    MatDatepickerModule,
  ]
})
export class ReportsModule { }
