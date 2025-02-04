import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { Charge } from 'app/shared/models/charge';
import { UserData } from 'app/shared/models/user-data';
import { AgentbakingService } from 'app/shared/services/agentbaking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { StorageService } from 'app/shared/services/storage.service';
import { SystemAdminService } from 'app/shared/services/system-admin.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-users-search',
  templateUrl: './users-search.component.html',
  styleUrls: ['./users-search.component.scss']
})
export class UsersSearchComponent implements OnInit {

  customerType = [];
  data: UserData[];
  selectedData: UserData;
  loading = [false, false, false, false]
  form: FormGroup;
  constructor(private router: Router,
    private fb: FormBuilder,
    private storage: StorageService,
    private alertService: AlertService,
    public dialogService: DialogService, 
    public messageService: MessageService,
    private systemAdminService: SystemAdminService,
  ) {

  }

  ngOnInit() {
    this.createForms();
    this.onSearch();
  }

  createForms() {
    this.form = this.fb.group({
      fullName: [null]      
    });
  }

  get f() {
    return this.form.controls;
  }


  onSearch() {
    this.f.fullName.setValue(this.f.fullName.value == "" ? null : this.f.fullName.value);
    this.loading[0] = true;
    this.systemAdminService.findUsers(this.form.value)
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
