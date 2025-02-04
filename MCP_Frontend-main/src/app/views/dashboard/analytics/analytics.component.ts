import { SelectionModel } from "@angular/cdk/collections";
import {
  Component,
  OnInit,
  AfterViewInit
} from "@angular/core";
import { MatTableDataSource } from "@angular/material/table";
import { matxAnimations } from "app/shared/animations/matx-animations";
import { ActiveOutlet } from "app/shared/models/active-outlet";
import { DashboardPieChartData } from "app/shared/models/pie-chart-data";
import { SearchCriteria } from "app/shared/models/search-criteria";
import { LoaderService } from "app/shared/services/loader.service";
import { ReportService } from "app/shared/services/reports.service";
import { SecurityService } from "app/shared/services/security.service";
import { ThemeService } from "app/shared/services/theme.service";
import { DashboardCustomerCategoryComponent } from "app/views/agent-banking/dashboard-customer-category/dashboard-customer-category.component";
import { DashboardOutletFloatLevelComponent } from "app/views/agent-banking/dashboard-outlet-float-level/dashboard-outlet-float-level.component";
import { ChartOptions, ChartType } from "chart.js";
import { Label, SingleDataSet } from "ng2-charts";
import { DialogService, DynamicDialogRef } from "primeng/dynamicdialog";
import { first } from "rxjs/operators";


@Component({
  selector: "app-analytics",
  templateUrl: "./analytics.component.html",
  styleUrls: ["./analytics.component.scss"],
  animations: matxAnimations
})
export class AnalyticsComponent implements OnInit, AfterViewInit {
  loaderText = "Extracting Data Please wait...";
  loaderColor = "blue";
  displayedColumns: string[] = ['outletNo', 'outletName', 'transCount'];
  todayDatasource = new MatTableDataSource<ActiveOutlet>();
  monthDatasource = new MatTableDataSource<ActiveOutlet>();
  selection = new SelectionModel<ActiveOutlet>(false, []);
  loading = [false, false, false];
  // Pie
  public pieChartOptions: ChartOptions = {
    responsive: true
  };
  // public pieChartLabels: Label[] = [
  //   'Download',
  //   'Store',
  //   'Mail Sales'
  // ];
  //public pieChartData: SingleDataSet = [300, 500, 100];
  public pieChartType: ChartType = 'pie';
  public pieChartLegend = false;
  public pieChartPlugins = [];

  public floatStatusChartLabels: Label[] = []
  public floatStatusChartValues: SingleDataSet = [0, 0, 0];

  public customerBalanceChartLabels: Label[] = []
  public customerBalanceChartValues: SingleDataSet = [0, 0, 0];

  public activeCustomerChartLabels: Label[] = []
  public activeCustomerChartValues: SingleDataSet = [0, 0, 0];

  statCardList = [
    {
      icon: "people",
      title: "New Agents - Current Month",
      amount: "0",
      color: "primary"
    },
    {
      icon: "attach_money",
      title: "New Outlets - Current Month",
      amount: "0",
      color: "warn"
    },
    {
      icon: "store",
      title: "Micropay Commission-Today",
      amount: "0",
      color: "accent"
    },
    {
      icon: "shopping_cart",
      title: "Total Expenses - Today",
      amount: "0",
      color: "default"
    }
  ];



  ref: DynamicDialogRef;
  constructor(private reportService: ReportService,
    private customLoader: LoaderService,
    public dialogService: DialogService) { }
  ngAfterViewInit() { }
  ngOnInit() {
    this.findDashboardStatistics();
    this.findAgentFloatLevels();
    this.findCustomerAccountBalanceLevels();
    this.findActiveCustomerCategories();
    this.findActiveAgentsByTransToday();
    this.findActiveAgentsByTransMonth();
  }

  findDashboardStatistics() {
    //this.loading = true;
    let search = new SearchCriteria;
    this.reportService.findDashboardStatistics(search)
      .pipe(first())
      .subscribe(response => {
        if (response.code !== "00")
          return;
        this.statCardList = response.data;
      });
  }

  findAgentFloatLevels() {
    this.loading[0] = true;
    let search = new SearchCriteria;
    this.reportService.findAgentFloatLevels(search)
      .pipe(first())
      .subscribe(response => {
        this.loading[0] = false;
        if (response.code !== "00")
          return;
        let data = new DashboardPieChartData;
        data = response.data;
        this.floatStatusChartLabels = data.lables;
        this.floatStatusChartValues = data.values;
        console.log(this.floatStatusChartValues);
      });
  }


  findCustomerAccountBalanceLevels() {
    this.loading[1] = true;
    let search = new SearchCriteria;
    this.reportService.findCustomerAccountBalanceLevels(search)
      .pipe(first())
      .subscribe(response => {
        this.loading[1] = false;
        if (response.code !== "00")
          return;
        let data = new DashboardPieChartData;
        data = response.data;
        this.customerBalanceChartLabels = data.lables;
        this.customerBalanceChartValues = data.values;
      });
  }


  findActiveCustomerCategories() {
    this.loading[2] = true;
    let search = new SearchCriteria;
    this.reportService.findActiveCustomerCategories(search)
      .pipe(first())
      .subscribe(response => {
        this.loading[2] = false;
        if (response.code !== "00")
          return;
        let data = new DashboardPieChartData;
        data = response.data;
        this.activeCustomerChartLabels = data.lables;
        this.activeCustomerChartValues = data.values;
      });
  }

  findActiveAgentsByTransToday() {
    //this.loading = true;
    this.customLoader.start({ text: this.loaderText, fgsColor: this.loaderColor });
    let search = new SearchCriteria;
    search.scope = 'DAILY'
    try {
      this.reportService.findActiveAgentsByTransactions(search)
        .pipe(first())
        .subscribe(data => {
          this.todayDatasource.data = data.data as ActiveOutlet[];
          this.customLoader.stop();
          //this.loading = false;
        });
    } catch (e) {
      this.customLoader.stop();
    }
  }

  findActiveAgentsByTransMonth() {
    //this.loading = true;
    this.customLoader.start({ text: this.loaderText, fgsColor: this.loaderColor });
    let search = new SearchCriteria;
    search.scope = 'MONTHLY'
    try {
      this.reportService.findActiveAgentsByTransactions(search)
        .pipe(first())
        .subscribe(data => {
          this.monthDatasource.data = data.data as ActiveOutlet[];
          this.customLoader.stop();
          //this.loading = false;
        });
    } catch (e) {
      this.customLoader.stop();
    }
  }


  onFloatStatusClicked(e: any, pieChartType: string) {
    let chartLabel: string = e.active[0]._model.label;
    console.log(chartLabel);
    let searchCriteria = new SearchCriteria;

    if (pieChartType == 'ACTIVE_CUSTOMERS') {
      searchCriteria.scope = chartLabel.toUpperCase();
      this.ref = this.dialogService.open(DashboardCustomerCategoryComponent, {
        header: 'Customers',
        width: '50%',
        closable: true,
        styleClass: "p-dialog-titlebar",
        contentStyle: { "max-height": "500px", "overflow": "auto" },
        data: searchCriteria,
      });
    } else {
      searchCriteria.scope = chartLabel;
      if (pieChartType == 'OUTLET_FLOAT_STATUS')
        searchCriteria.categories = 'FLOAT_ACCOUNT';
      if (pieChartType == 'CUSTOMER_BALANCE')
        searchCriteria.categories = 'CUSTOMER';
      this.ref = this.dialogService.open(DashboardOutletFloatLevelComponent, {
        header: 'Float Balances',
        width: '50%',
        closable: true,
        styleClass: "p-dialog-titlebar",
        contentStyle: { "max-height": "500px", "overflow": "auto" },
        data: searchCriteria,
      });
    }
    this.ref.onClose.subscribe(() => {
    });
  }


}
