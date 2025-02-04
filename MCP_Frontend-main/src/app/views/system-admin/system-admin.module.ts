import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SystemAdminRoutingModule } from './system-admin-routing.module';
import { UserRolesSearchComponent } from './user-roles-search/user-roles-search.component';
import { UserRolesMaintainComponent } from './user-roles-maintain/user-roles-maintain.component';
import { UsersMaintainComponent } from './users-maintain/users-maintain.component';
import { UsersSearchComponent } from './users-search/users-search.component';
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
import { SystemParameterMaintainComponent } from './system-parameter-maintain/system-parameter-maintain.component';
import { SystemParameterSearchComponent } from './system-parameter-search/system-parameter-search.component';
import { ServiceChannelSearchComponent } from './service-channel-search/service-channel-search.component';
import { ServiceChannelMaintainComponent } from './service-channel-maintain/service-channel-maintain.component';
import { UserRoleAccessRightComponent } from './user-role-access-right/user-role-access-right.component';



@NgModule({
  declarations: [
    UserRolesSearchComponent,
    UserRolesMaintainComponent,
    UsersMaintainComponent,
    UsersSearchComponent,
    SystemParameterMaintainComponent,SystemParameterSearchComponent, ServiceChannelSearchComponent, ServiceChannelMaintainComponent, UserRoleAccessRightComponent, 
  ],
  imports: [
    CommonModule,
    SystemAdminRoutingModule,
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
  ]
})
export class SystemAdminModule { }
