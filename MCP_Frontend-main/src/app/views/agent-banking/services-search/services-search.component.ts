import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TransCode } from 'app/shared/models/trans-code';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-services-search',
  templateUrl: './services-search.component.html',
  styleUrls: ['./services-search.component.scss']
})
export class ServicesSearchComponent implements OnInit {
  customerType = [];
  data: TransCode[];
  selectedData: TransCode;
  loading = [false, false, false, false]

  constructor(private router: Router,
    private storage: StorageService,
    private alertService: AlertService,
    public dialogService: DialogService, 
    public messageService: MessageService,
    private globalService: AgentBankingService,
  ) {

  }

  ngOnInit() {
    this.onSearch();
  }

  onSearch() {
    let request = new TransCode;
    this.loading[0] = true;
    this.globalService.findServices(request)
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
    this.storage.setSelectedProduct(this.selectedData);
    this.router.navigate(['agent-banking/services', 'Edit']);
  }

  onView() {  
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to view');
      return;
    }
    this.storage.setSelectedProduct(this.selectedData);
    this.router.navigate(['agent-banking/services', 'View']);
  }

  onAdd() {
    this.router.navigate(['agent-banking/services', 'Add']);
  }

}
