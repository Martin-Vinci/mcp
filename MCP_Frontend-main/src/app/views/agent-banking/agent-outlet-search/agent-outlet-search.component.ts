import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MobileUser } from 'app/shared/models/mobile-user';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { RouterServiceService } from 'app/shared/services/router-service.service';
import { StorageService } from 'app/shared/services/storage.service';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-agent-outlet-search',
  templateUrl: './agent-outlet-search.component.html',
  styleUrls: ['./agent-outlet-search.component.scss']
})
export class AgentOutletSearchComponent implements OnInit {
  customerType = [];
  data: MobileUser[];
  selectedData: MobileUser;
  product: MobileUser;
  loading = [false, false, false, false]
  constructor(private router: Router,
    private storage: StorageService,
    private alertService: AlertService,
    private navigation: RouterServiceService,
    public dialogService: DialogService,
    private depositService: AgentBankingService,
  ) {

  }

  ngOnInit() {
    this.product = this.storage.getSelectedProduct();
    this.onSearch();
  }


  onSearch() {
    let request = new MobileUser;
    request.entityCode = this.product.entityCode;
    this.loading[0] = true;   
    this.depositService.findOutlets(request)
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


  onAdd() {
    this.router.navigate(['agent-banking/agent-outlet', 'Add']);
  }

  onEdit() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to modify');
      return;
    }
    this.storage.setEntity(this.selectedData);
    this.router.navigate(['agent-banking/agent-outlet', 'Edit']);
  }

  onView() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to view');
      return;
    }
    this.storage.setEntity(this.selectedData);
    this.router.navigate(['agent-banking/agent-outlet', 'View']);
  }


  onClose() {
    this.navigation.goBack()
  }

}
