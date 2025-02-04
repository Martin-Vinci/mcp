import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Biller } from 'app/shared/models/biller';
import { TransCode } from 'app/shared/models/trans-code';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { BillerService } from 'app/shared/services/biller.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-biller-search',
  templateUrl: './biller-search.component.html',
  styleUrls: ['./biller-search.component.scss']
})
export class BillerSearchComponent implements OnInit {
  data: Biller[];
  selectedData: Biller;
  loading = [false, false, false, false]

  constructor(private router: Router,
    private storage: StorageService,
    private alertService: AlertService,
    public dialogService: DialogService, 
    public messageService: MessageService,
    private globalService: BillerService,
  ) {

  }

  ngOnInit() {
    this.onSearch();
  }

  onSearch() {
    let request = new Biller;
    this.loading[0] = true;
    this.globalService.findBillers(request)
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
    this.router.navigate(['biller-control/biller', 'Edit']);
  }

  onView() {  
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to view');
      return;
    }
    this.storage.setSelectedProduct(this.selectedData);
    this.router.navigate(['biller-control/biller', 'View']);
  }

  onAdd() {
    this.router.navigate(['biller-control/biller', 'Add']);
  }

}
