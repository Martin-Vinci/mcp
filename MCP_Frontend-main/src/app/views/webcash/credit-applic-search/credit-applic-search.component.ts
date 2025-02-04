import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Customer } from 'app/shared/models/customer';
import { CreditAppl } from 'app/shared/models/loan-application';
import { TransCode } from 'app/shared/models/trans-code';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { CustomerService } from 'app/shared/services/customer-service.service';
import { LoanServiceService } from 'app/shared/services/loan-service.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-credit-applic-search',
  templateUrl: './credit-applic-search.component.html',
  styleUrls: ['./credit-applic-search.component.scss']
})
export class CreditApplicSearchComponent implements OnInit {
  customerType = [];
  data: CreditAppl[];
  selectedData: CreditAppl;
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
    let request = new CreditAppl;
    request.status = "ACTIVE";
    this.loading[0] = true;
    this.globalService.findAllCreditApplications(request)
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
    this.router.navigate(['webcash/credit-appl', 'Add']);
  }

  public onView() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('No record has been selected');
      return;
    }
    this.storageService.setEntity(this.selectedData);
    this.router.navigate(['webcash/credit-appl', 'View']);
  }

  public onEdit() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('No record has been selected');
      return;
    }
    this.storageService.setEntity(this.selectedData);
    this.router.navigate(['webcash/credit-appl', 'Edit']);
  }

}
