import { Component, OnInit } from '@angular/core';
import { MobileUserAcct } from 'app/shared/models/mobile-user-acct';
import { SearchCriteria } from 'app/shared/models/search-criteria';
import { TransData } from 'app/shared/models/transaction-ref';
import { ReportService } from 'app/shared/services/reports.service';
import { MessageService } from 'primeng/api';
import { DialogService, DynamicDialogRef, DynamicDialogConfig } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard-outlet-float-level',
  templateUrl: './dashboard-outlet-float-level.component.html',
  styleUrls: ['./dashboard-outlet-float-level.component.scss']
})
export class DashboardOutletFloatLevelComponent implements OnInit {

  isTableLoading = false;
  loading = false;
  errorMessage: string;
  searchCriteria = new SearchCriteria;
  data: MobileUserAcct[];
  selectedData: MobileUserAcct;
  public value: moment.Moment;
  constructor(
    public dialogService: DialogService,
    public messageService: MessageService,
    public ref: DynamicDialogRef,
    public config: DynamicDialogConfig,
    private agentBankingService: ReportService
  ) {

  }

  ngOnInit() {
    this.searchCriteria = this.config.data;
    this.getAccounts();
  }


  getAccounts() {
    this.loading = true;
    this.agentBankingService.findUserAccountsByCategoryAndFloatLevels(this.searchCriteria)
      .pipe(first())
      .subscribe(
        response => {
          this.loading = false;
          if (response.code !== "00") {
            //this.alertService.displayError(response.message);
            return;
          }
          this.data = response.data;
        },
        error => {
          this.loading = false;
        });
  }

  cancel() {
    this.ref.close(false);
  }
}