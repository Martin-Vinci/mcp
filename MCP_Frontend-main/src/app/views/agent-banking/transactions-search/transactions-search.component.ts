import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Charge } from 'app/shared/models/charge';
import { TransactionRef } from 'app/shared/models/transaction-ref';
import { UserData } from 'app/shared/models/user-data';
import { AgentbakingService } from 'app/shared/services/agentbaking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { StorageService } from 'app/shared/services/storage.service';
import { SystemAdminService } from 'app/shared/services/system-admin.service';
import { TransactionService } from 'app/shared/services/transaction.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-transactions-search',
  templateUrl: './transactions-search.component.html',
  styleUrls: ['./transactions-search.component.scss']
})
export class TransactionsSearchComponent implements OnInit {

  customerType = [];
  data: UserData[];
  selectedData: UserData;
  loading = [false, false, false, false]

  constructor(private router: Router,
    private storage: StorageService,
    private alertService: AlertService,
    public dialogService: DialogService, 
    public messageService: MessageService,
    private transactionService: TransactionService,
  ) {

  }

  ngOnInit() {
    this.onSearch();
  }


  onSearch() {
    let request = new TransactionRef;
    this.loading[0] = true;
    this.transactionService.findTransactions(request)
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


  onEdit() { 
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to modify');
      return;
    }
    this.storage.setEntity(this.selectedData);
    this.router.navigate(['system-admin/user', 'Edit']);
  }

  onView() {  
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to view');
      return;
    }
    this.storage.setEntity(this.selectedData);
    this.router.navigate(['system-admin/user', 'View']);
  }

  onAdd() {
    this.router.navigate(['system-admin/user', 'Add']);
  }


}
