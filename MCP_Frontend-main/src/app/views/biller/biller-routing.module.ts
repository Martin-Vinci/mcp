import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BillerCategoryMaintainComponent } from './biller-category-maintain/biller-category-maintain.component';
import { BillerCategorySearchComponent } from './biller-category-search/biller-category-search.component';
import { BillerMaintainComponent } from './biller-maintain/biller-maintain.component';
import { BillerProductMaintainComponent } from './biller-product-maintain/biller-product-maintain.component';
import { BillerProductSearchComponent } from './biller-product-search/biller-product-search.component';
import { BillerSearchComponent } from './biller-search/biller-search.component';
import { CustomSmsBodyComponent } from './custom-sms-body/custom-sms-body.component';
import { MessagesComponent } from './messages/messages.component';
import { NotificationsSearchComponent } from './notifications-search/notifications-search.component';


const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'biller-search', component: BillerSearchComponent,
        data: { title: "Billers", breadcrumb: "Billers" }
      },
      {
        path: 'biller/:id', component: BillerMaintainComponent,
        data: { title: "Biller Details", breadcrumb: "Biller Details" }
      },
      {
        path: 'notification-search', component: NotificationsSearchComponent,
        data: { title: "Payment Notifications", breadcrumb: "Payment Notifications" }
      },
      {
        path: 'sms-body', component: CustomSmsBodyComponent,
        data: { title: "SMS Body", breadcrumb: "SMS Body" }
      }, 
      {
        path: 'messages-search', component: MessagesComponent,
        data: { title: "Messages", breadcrumb: "Messages" }
      },   
      
      
      {
        path: 'biller-category/:id', component: BillerCategoryMaintainComponent,
        data: { title: "Messages", breadcrumb: "Messages" }
      },  
      {
        path: 'biller-category-search', component: BillerCategorySearchComponent,
        data: { title: "Messages", breadcrumb: "Messages" }
      },  
      {
        path: 'biller-product/:id', component: BillerProductMaintainComponent,
        data: { title: "Messages", breadcrumb: "Messages" }
      },  
      {
        path: 'biller-product-search', component: BillerProductSearchComponent,
        data: { title: "Messages", breadcrumb: "Messages" }
      },  




   ]
  }
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BillerRoutingModule { }
