import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ServiceChannelMaintainComponent } from './service-channel-maintain/service-channel-maintain.component';
import { ServiceChannelSearchComponent } from './service-channel-search/service-channel-search.component';
import { SystemParameterMaintainComponent } from './system-parameter-maintain/system-parameter-maintain.component';
import { SystemParameterSearchComponent } from './system-parameter-search/system-parameter-search.component';
import { UserRoleAccessRightComponent } from './user-role-access-right/user-role-access-right.component';
import { UserRolesMaintainComponent } from './user-roles-maintain/user-roles-maintain.component';
import { UserRolesSearchComponent } from './user-roles-search/user-roles-search.component';
import { UsersMaintainComponent } from './users-maintain/users-maintain.component';
import { UsersSearchComponent } from './users-search/users-search.component';


const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'user-role-search', component: UserRolesSearchComponent,
        data: { title: "User Roles List", breadcrumb: "User Roles List" }
      },
      {
        path: 'user-role/:id', component: UserRolesMaintainComponent,
        data: { title: "User Role Details", breadcrumb: "User Role Details" }
      },
      {
        path: "user-role-access",
        component: UserRoleAccessRightComponent,
        data: { title: "Charge Type Search" },
      },
      {
        path: 'system-parameter', component: SystemParameterSearchComponent,
        data: { title: "System Parameters", breadcrumb: "System Parameters" }
      },
      {
        path: 'system-parameter/:id', component: SystemParameterMaintainComponent,
        data: { title: "Maintain Parameters", breadcrumb: "Maintain Parameters" }
      },
      {
        path: 'user-search', component: UsersSearchComponent,
        data: { title: "Users Listing", breadcrumb: "Users Listing" }
      },
      {
        path: 'user/:id', component: UsersMaintainComponent,
        data: { title: "User Details", breadcrumb: "User Details" }
      },

      {
        path: 'service-channel-search', component: ServiceChannelSearchComponent,
        data: { title: "Service Channel Listing", breadcrumb: "Service Channel" }
      },
      {
        path: 'service-channel/:id', component: ServiceChannelMaintainComponent,
        data: { title: "Service Channel Maintain", breadcrumb: "Maintain Service Channel" }
      },


   ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SystemAdminRoutingModule { }
