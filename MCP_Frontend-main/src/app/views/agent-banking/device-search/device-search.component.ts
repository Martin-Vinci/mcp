import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Charge } from 'app/shared/models/charge';
import { AgentbakingService } from 'app/shared/services/agentbaking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-device-search',
  templateUrl: './device-search.component.html',
  styleUrls: ['./device-search.component.scss']
})
export class DeviceSearchComponent implements OnInit {

  customerType = [];
  data: Charge[];
  selectedData: Charge;
  loading = [false, false, false, false]

  constructor(private router: Router,
    private storage: StorageService,
    private alertService: AlertService,
    public dialogService: DialogService, 
    public messageService: MessageService,
    private globalService: AgentbakingService,
  ) {

  }

  ngOnInit() {
    this.onSearch();
  }

  onSearch() {
    let request = new Charge;
    this.loading[0] = true;
    this.globalService.findCharges(request)
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
    this.storage.setSelectedProduct(this.selectedData);
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to modify');
      return;
    }
    this.router.navigate(['agent-banking/services', 'Edit']);
  }

  onView() {
    this.storage.setSelectedProduct(this.selectedData);
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to view');
      return;
    }
    this.router.navigate(['agent-banking/services', 'View']);
  }

  onAdd() {
    this.router.navigate(['agent-banking/services', 'Add']);
  }


}
