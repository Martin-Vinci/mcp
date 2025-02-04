import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EscrowTransactionsComponent } from './escrow-transactions/escrow-transactions.component';
import { GoodsAndServicesListingComponent } from './goods-and-services-listing/goods-and-services-listing.component';
import { GoodsAndServicesMaintainComponent } from './goods-and-services-maintain/goods-and-services-maintain.component';
import { SupplierPaymentHistoryComponent } from './supplier-payment-history/supplier-payment-history.component';
import { SupplierPaymentComponent } from './supplier-payment/supplier-payment.component';
import { TransactionsDetailsComponent } from './transactions-details/transactions-details.component';
import { TransactionsSearchComponent } from './transactions-search/transactions-search.component';
import { VoucherSearchComponent } from './voucher-search/voucher-search.component';
import { CenteTrustTransactionListingComponent } from './cente-trust-transaction-listing/cente-trust-transaction-listing.component';
import { CenteTrustTransactionSummaryComponent } from './cente-trust-transaction-summary/cente-trust-transaction-summary.component';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'transaction-search', component: TransactionsSearchComponent,
        data: { title: "Transaction List", breadcrumb: "Transaction List" }
      },
      {
        path: 'transaction/:id', component: TransactionsDetailsComponent,
        data: { title: "Transaction Details", breadcrumb: "Biller Details" }
      },
      {
        path: 'cente-trust-trans-search', component: CenteTrustTransactionListingComponent,
        data: { title: "Cente Trust Transaction", breadcrumb: "Cente Trust Transaction" }
      },   
      {
        path: 'cente-trust-trans-summary', component: CenteTrustTransactionSummaryComponent,
        data: { title: "Cente Trust Transaction", breadcrumb: "Cente Trust Transaction" }
      },    
      {
        path: 'voucher-search', component: VoucherSearchComponent,
        data: { title: "Voucher Requests", breadcrumb: "Voucher Requests" }
      },  
      {
        path: 'escrow-trans-search', component: EscrowTransactionsComponent,
        data: { title: "Escrow Transactions", breadcrumb: "Escrow Transactions" }
      },  
      {
        path: 'supplier-payment/:id', component: SupplierPaymentComponent,
        data: { title: "EFRIS-Supplier Payment", breadcrumb: "EFRIS-Supplier Payment" }
      },    
      {
        path: 'supplier-payment-history', component: SupplierPaymentHistoryComponent,
        data: { title: "EFRIS-Supplier Payment", breadcrumb: "EFRIS-Supplier Payment" }
      }, 

      {
        path: 'efris-service-listing', component: GoodsAndServicesListingComponent,
        data: { title: "EFRIS-Service Listing", breadcrumb: "Goods and services" }
      },    
      {
        path: 'efris-services/:id', component: GoodsAndServicesMaintainComponent,
        data: { title: "EFRIS-Service Maintain", breadcrumb: "Goods and services" }
      }, 
   ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TransactionsRoutingModule { }
