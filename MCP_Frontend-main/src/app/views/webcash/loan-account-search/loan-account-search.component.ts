import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LoanAccount } from 'app/shared/models/loan-account';
import { CreditAppl } from 'app/shared/models/loan-application';
import { AlertService } from 'app/shared/services/alert.service';
import { LoanServiceService } from 'app/shared/services/loan-service.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-loan-account-search',
  templateUrl: './loan-account-search.component.html',
  styleUrls: ['./loan-account-search.component.scss']
})
export class LoanAccountSearchComponent implements OnInit {
  customerType = [];
  data: LoanAccount[];
  selectedData: LoanAccount;
  loading = [false, false, false, false]

  constructor(private router: Router,
    private storageService: StorageService,
    private alertService: AlertService,
    public dialogService: DialogService, 
    public messageService: MessageService,
    private globalService: LoanServiceService,
  ) {

  }

  ngOnInit() {
    this.onSearch();
  }

  onSearch() {
    let request = new LoanAccount;
    this.loading[0] = true;
    this.globalService.findLoanAccounts(request)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.data = response.data;
        },
        error => {
          this.loading[0] = false;
          this.alertService.displayError(error);
        });
  }


  public onAdd() {
    this.router.navigate(['webcash/loan-account', 'Add']);
  }

  public onView() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('No record has been selected');
      return;
    }
    this.storageService.setEntity(this.selectedData);
    this.router.navigate(['webcash/loan-account', 'View']);
  }

  public onEdit() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('No record has been selected');
      return;
    }
    this.storageService.setEntity(this.selectedData);
    this.router.navigate(['webcash/loan-account', 'Edit']);
  }

}
