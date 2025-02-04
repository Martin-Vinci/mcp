import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AgentMaintainComponent } from './agent-maintain/agent-maintain.component';
import { AgentOutletMaintainComponent } from './agent-outlet-maintain/agent-outlet-maintain.component';
import { AgentOutletSearchComponent } from './agent-outlet-search/agent-outlet-search.component';
import { AgentSearchComponent } from './agent-search/agent-search.component';
import { MobileUserListingComponent } from './mobile-user-listing/mobile-user-listing.component';
import { MobileUserMaintainComponent } from './mobile-user-maintain/mobile-user-maintain.component';
import { MobileUserReviewComponent } from './mobile-user-review/mobile-user-review.component';
import { ServicesChargeComponent } from './services-charge/services-charge.component';
import { ServicesCommissionComponent } from './services-commission/services-commission.component';
import { ServicesMaintainComponent } from './services-maintain/services-maintain.component';
import { ServicesPostingListComponent } from './services-posting-list/services-posting-list.component';
import { ServicesPostingMaintainComponent } from './services-posting-maintain/services-posting-maintain.component';
import { ServicesSearchComponent } from './services-search/services-search.component';



const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'services-list', component: ServicesSearchComponent,
        data: { title: "Services", breadcrumb: "Services" }
      },
      {
        path: 'services/:id', component: ServicesMaintainComponent,
        data: { title: "Maintain Service", breadcrumb: "Maintain Service" }
      },
      {
        path: 'agent-search', component: AgentSearchComponent,
        data: { title: "Agent", breadcrumb: "Agent" }
      },
      {
        path: 'main-agent/:id', component: AgentMaintainComponent,
        data: { title: "Maintain Agent", breadcrumb: "Maintain Agent" }
      },
      {
        path: 'agent-outlet', component: AgentOutletSearchComponent,
        data: { title: "Agent Outlet", breadcrumb: "Agent Outlet" }
      },

      {
        path: 'agent-outlet/:id', component: AgentOutletMaintainComponent,
        data: { title: "Agent Outlet", breadcrumb: "Agent Outlet" }
      },

      {
        path: 'service-commission/:id', component: ServicesCommissionComponent,
        data: { title: "Service Charge", breadcrumb: "Service Charge" }
      },

      {
        path: 'service-charge/:id', component: ServicesChargeComponent,
        data: { title: "Service Commission", breadcrumb: "Service Commission" }
      },
      {
        path: 'service-posting-policy/:id', component: ServicesPostingMaintainComponent,
        data: { title: "Service Posting Policy", breadcrumb: "Service Posting Policy" }
      },
      {
        path: 'service-posting-policy', component: ServicesPostingListComponent,
        data: { title: "Service Posting Policy", breadcrumb: "Service Posting Policy" }
      },

      {
        path: 'mobile-user-search', component: MobileUserListingComponent,
        data: { title: "Agent Outlet", breadcrumb: "Mobile Users Listing" }
      },
      {
        path: 'mobile-user-maintain/:id', component: MobileUserMaintainComponent,
        data: { title: "Mobile User", breadcrumb: "Mobile Users" }
      },
      {
        path: 'mobile-user-review', component: MobileUserReviewComponent,
        data: { title: "User Approval", breadcrumb: "User Approval" }
      },
   ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AgentBankingRoutingModule { }
