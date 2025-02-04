import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Charge } from 'app/shared/models/charge';
import { ControlParameter } from 'app/shared/models/control-parameter';
import { AgentbakingService } from 'app/shared/services/agentbaking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { StorageService } from 'app/shared/services/storage.service';
import { SystemAdminService } from 'app/shared/services/system-admin.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-system-parameter-search',
  templateUrl: './system-parameter-search.component.html',
  styleUrls: ['./system-parameter-search.component.scss']
})
export class SystemParameterSearchComponent implements OnInit {

  customerType = [];
  data: ControlParameter[];
  selectedData: ControlParameter;
  loading = [false, false, false, false]

  constructor(private router: Router,
    private storage: StorageService,
    private alertService: AlertService,
    public dialogService: DialogService, 
    public messageService: MessageService,
    private globalService: SystemAdminService,
  ) {

  }

  ngOnInit() {
    this.onSearch();
  }

  onSearch() {
    let request = new ControlParameter;
    this.loading[0] = true;
    this.globalService.findParameters(request)
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
    this.router.navigate(['system-admin/system-parameter', 'Edit']);
  }

  onView() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to view');
      return;
    }
    this.storage.setEntity(this.selectedData);
    this.router.navigate(['system-admin/system-parameter', 'View']);
  }

  onAdd() {
    this.router.navigate(['system-admin/system-parameter', 'Add']);
  }

}