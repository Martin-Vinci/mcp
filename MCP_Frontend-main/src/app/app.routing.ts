import { Routes } from "@angular/router";
import { AdminLayoutComponent } from "./shared/components/layouts/admin-layout/admin-layout.component";
import { AuthLayoutComponent } from "./shared/components/layouts/auth-layout/auth-layout.component";
import { AuthGuard } from "./shared/guards/auth.guard";

export const rootRouterConfig: Routes = [
  // {
  //   path: "",
  //   redirectTo: "/dashboard/analytics",
  //   pathMatch: "full",
  // },
  {
    path: "",
    component: AuthLayoutComponent,
    children: [
      {
        path: "sessions",
        loadChildren: () =>
          import("./views/sessions/sessions.module").then(
            (m) => m.SessionsModule
          ),
        data: { title: "Session" },
      },
    ],
  },
  {
    path: "",
    component: AdminLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: "dashboard",
        loadChildren: () =>
          import("./views/dashboard/dashboard.module").then((m) => m.DashboardModule), canActivate: [AuthGuard]
      },
      {
        path: "system-admin",
        loadChildren: () =>
          import("./views/system-admin/system-admin.module").then((m) => m.SystemAdminModule),
        data: { title: "Agent Banking", breadcrumb: "Agent Banking" },canActivate: [AuthGuard]
      },
      
      {
        path: "agent-banking",
        loadChildren: () =>
          import("./views/agent-banking/agent-banking.module").then((m) => m.AgentBankingModule),
        data: { title: "Agent Banking", breadcrumb: "Agent Banking" },canActivate: [AuthGuard]
      },
      {
        path: "biller-control",
        loadChildren: () =>
          import("./views/biller/biller.module").then((m) => m.BillerModule),
        data: { title: "Biller Control", breadcrumb: "Biller Control" },canActivate: [AuthGuard]
      },
      {
        path: "webcash",
        loadChildren: () =>
          import("./views/webcash/webcash.module").then((m) => m.WebcashModule),
        data: { title: "Web Cash", breadcrumb: "Web Cash" },canActivate: [AuthGuard]
      },
      {
        path: "transaction-control",
        loadChildren: () =>
          import("./views/transactions/transactions.module").then((m) => m.TransactionsModule),
        data: { title: "Transactions Control", breadcrumb: "Transactions Control" },canActivate: [AuthGuard]
      },       
      {
        path: "report-control",
        loadChildren: () =>
          import("./views/reports/reports.module").then((m) => m.ReportsModule),
        data: { title: "Transactions Control", breadcrumb: "Transactions Control" },canActivate: [AuthGuard]
      }, 
    ],
  },
  {
    path: "**",
    redirectTo: "sessions/404",
  },
];
