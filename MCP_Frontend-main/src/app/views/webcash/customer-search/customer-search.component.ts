import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Customer } from 'app/shared/models/customer';
import { TransCode } from 'app/shared/models/trans-code';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { CustomerService } from 'app/shared/services/customer-service.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-customer-search',
  templateUrl: './customer-search.component.html',
  styleUrls: ['./customer-search.component.scss']
})
export class CustomerSearchComponent implements OnInit {
  customerType = [];
  data: Customer[];
  selectedData: Customer;
  loading = [false, false, false, false]

  constructor(private router: Router,
    private storageService: StorageService,
    private alertService: AlertService,
    public dialogService: DialogService, 
    public messageService: MessageService,
    private globalService: CustomerService,
  ) {

  }

  ngOnInit() {
    this.onSearch();
  }

  onSearch() {
    let request = new Customer;
    this.loading[0] = true;
    this.globalService.findAllCustomers(request)
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
    this.router.navigate(['webcash/customer', 'Add']);
  }

  public onView() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('No record has been selected');
      return;
    }
    this.storageService.setEntity(this.selectedData);
    this.router.navigate(['webcash/customer', 'View']);
  }

  public onEdit() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('No record has been selected');
      return;
    }
    this.storageService.setEntity(this.selectedData);
    this.router.navigate(['webcash/customer', 'Edit']);
  }

}
