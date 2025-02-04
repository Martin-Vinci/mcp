import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AgentBankingRoutingModule } from './agent-banking-routing.module';
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
import {TableModule} from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { PanelModule } from 'primeng/panel';
import { MenuModule } from 'primeng/menu';
import { ServicesSearchComponent } from './services-search/services-search.component';
import { ServicesMaintainComponent } from './services-maintain/services-maintain.component';
import { ServicesChargeComponent } from './services-charge/services-charge.component';
import { ServicesChargeTiersComponent } from './services-charge-tiers/services-charge-tiers.component';
import { ServicesCommissionTiersComponent } from './services-commission-tiers/services-commission-tiers.component';
import { ServicesCommissionComponent } from './services-commission/services-commission.component';
import { ServicesPostingListComponent } from './services-posting-list/services-posting-list.component';
import { ServicesPostingMaintainComponent } from './services-posting-maintain/services-posting-maintain.component';
import { AgentSearchComponent } from './agent-search/agent-search.component';
import { AgentMaintainComponent } from './agent-maintain/agent-maintain.component';
import { AgentOutletMaintainComponent } from './agent-outlet-maintain/agent-outlet-maintain.component';
import { AgentOutletSearchComponent } from './agent-outlet-search/agent-outlet-search.component';
import { DeviceSearchComponent } from './device-search/device-search.component';
import { DeviceMaintainComponent } from './device-maintain/device-maintain.component';
import { MobileUserListingComponent } from './mobile-user-listing/mobile-user-listing.component';
import { MobileUserMaintainComponent } from './mobile-user-maintain/mobile-user-maintain.component';
import { EntityAccountModalComponent } from './entity-account-modal/entity-account-modal.component';
import { MobileUserReviewComponent } from './mobile-user-review/mobile-user-review.component';
import { ServiceCodeHeaderComponent } from './service-code-header/service-code-header.component';
import { DashboardOutletFloatLevelComponent } from './dashboard-outlet-float-level/dashboard-outlet-float-level.component';
import { DashboardCustomerCategoryComponent } from './dashboard-customer-category/dashboard-customer-category.component';
import { MobileUserReviewDetailsComponent } from './mobile-user-review-details/mobile-user-review-details.component';


@NgModule({
  declarations: [ServicesSearchComponent, ServicesMaintainComponent, ServicesChargeComponent, 
    ServicesChargeTiersComponent, ServicesCommissionTiersComponent, ServicesCommissionComponent, 
    ServicesPostingListComponent, ServicesPostingMaintainComponent, 
    AgentSearchComponent, AgentMaintainComponent, AgentOutletMaintainComponent, 
    AgentOutletSearchComponent, DeviceSearchComponent, 
    DeviceMaintainComponent, MobileUserListingComponent, MobileUserMaintainComponent, EntityAccountModalComponent, 
    MobileUserReviewComponent, ServiceCodeHeaderComponent, DashboardOutletFloatLevelComponent, 
    DashboardCustomerCategoryComponent,MobileUserReviewDetailsComponent],
  imports: [
    CommonModule,
    AgentBankingRoutingModule,
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
    MenuModule,
  ],
  exports: [
    ServiceCodeHeaderComponent
  ]
})
export class AgentBankingModule { }
