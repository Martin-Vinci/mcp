import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TransactionsRoutingModule } from './transactions-routing.module';
import { TransactionsSearchComponent } from './transactions-search/transactions-search.component';
import { TransactionsDetailsComponent } from './transactions-details/transactions-details.component';
import { VoucherSearchComponent } from './voucher-search/voucher-search.component';
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
import { MatIcon, MatIconModule } from '@angular/material/icon';
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
import { MenuModule } from 'primeng/menu';
import { PanelModule } from 'primeng/panel';
import { TableModule } from 'primeng/table';
import { EscrowTransactionsComponent } from './escrow-transactions/escrow-transactions.component';
import { SupplierPaymentHistoryComponent } from './supplier-payment-history/supplier-payment-history.component';
import { SupplierGoodsAndServicesComponent } from './supplier-goods-and-services/supplier-goods-and-services.component';
import { GoodsAndServicesListingComponent } from './goods-and-services-listing/goods-and-services-listing.component';
import {DividerModule} from 'primeng/divider';
import { GoodsAndServicesMaintainComponent } from './goods-and-services-maintain/goods-and-services-maintain.component';
import { EfrisCommoditiesComponent } from './efris-commodities/efris-commodities.component';
import { ModalEfrisGoodsAndServiceComponent } from './modal-efris-goods-and-service/modal-efris-goods-and-service.component';
import { SupplierPaymentComponent } from './supplier-payment/supplier-payment.component';
import { CenteTrustTransactionListingComponent } from './cente-trust-transaction-listing/cente-trust-transaction-listing.component';
import { CenteTrustTransModalComponent } from './cente-trust-trans-modal/cente-trust-trans-modal.component';
import { CenteTrustTransactionSummaryComponent } from './cente-trust-transaction-summary/cente-trust-transaction-summary.component';


@NgModule({
  declarations: [
    TransactionsSearchComponent,
    TransactionsDetailsComponent,
    VoucherSearchComponent,
    EscrowTransactionsComponent,
    SupplierPaymentComponent,
    SupplierPaymentHistoryComponent,
    SupplierGoodsAndServicesComponent,
    GoodsAndServicesListingComponent,
    GoodsAndServicesMaintainComponent,
    EfrisCommoditiesComponent,
    ModalEfrisGoodsAndServiceComponent,
    CenteTrustTransactionListingComponent,
    CenteTrustTransModalComponent,
    CenteTrustTransactionSummaryComponent
  ],
  imports: [
    CommonModule,
    TransactionsRoutingModule,
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
    DividerModule,
    MatIconModule,
    MenuModule
  ]
})
export class TransactionsModule { }
