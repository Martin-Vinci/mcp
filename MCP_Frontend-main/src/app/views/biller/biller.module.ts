import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { BillerRoutingModule } from './biller-routing.module';
import { BillerSearchComponent } from './biller-search/biller-search.component';
import { BillerMaintainComponent } from './biller-maintain/biller-maintain.component';
import { CustomSmsBodyComponent } from './custom-sms-body/custom-sms-body.component';
import { NotificationsSearchComponent } from './notifications-search/notifications-search.component';
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
import { MenuModule } from 'primeng/menu';
import { PanelModule } from 'primeng/panel';
import { TableModule } from 'primeng/table';
import { MessagesComponent } from './messages/messages.component';
import { BillerCategorySearchComponent } from './biller-category-search/biller-category-search.component';
import { BillerCategoryMaintainComponent } from './biller-category-maintain/biller-category-maintain.component';
import { BillerProductMaintainComponent } from './biller-product-maintain/biller-product-maintain.component';
import { BillerProductSearchComponent } from './biller-product-search/biller-product-search.component';
import { BillerHeaderComponent } from './biller-header/biller-header.component';


@NgModule({
  declarations: [
    BillerSearchComponent,
    BillerMaintainComponent,
    CustomSmsBodyComponent,
    NotificationsSearchComponent,
    MessagesComponent,
    BillerCategorySearchComponent,
    BillerCategoryMaintainComponent,
    BillerProductMaintainComponent,
    BillerProductSearchComponent,
    BillerHeaderComponent
  ],
  imports: [
    CommonModule,
    BillerRoutingModule,
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
    MenuModule
  ],
  exports: [
    BillerHeaderComponent
  ]
})
export class BillerModule { }
