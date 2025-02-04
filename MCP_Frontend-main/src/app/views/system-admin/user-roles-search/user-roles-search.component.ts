import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { ColumnMode, DatatableComponent, SelectionType } from '@swimlane/ngx-datatable';
import { UserTypeRef } from 'app/shared/models/user-type';
import { AlertService } from 'app/shared/services/alert.service';
import { SecurityService } from 'app/shared/services/security.service';
import { StorageService } from 'app/shared/services/storage.service';
import { SystemAdminService } from 'app/shared/services/system-admin.service';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-user-roles-search',
  templateUrl: './user-roles-search.component.html',
  styleUrls: ['./user-roles-search.component.scss']
})
export class UserRolesSearchComponent implements OnInit {

  @ViewChild(DatatableComponent) table: DatatableComponent;
  customerType = [];
  data: UserTypeRef[];
  selectedData: UserTypeRef;
  loading = [false, false, false, false]
  viewAccess = false;
  editAccess = false;
  addAccess = false;
  public columns = [
    { name: "id", prop: "id" },
    { name: "plotNumber", prop: "Plot Number" },
  ];
  private filterData = [];
  constructor(public router: Router,
    private admin: SystemAdminService,
    private storage: StorageService,
    private alertService: AlertService,
    private authService: SecurityService,
    ) {
    
  }

  ngOnInit() {
    this.determineAccessArights();
    this.onSearch();
  }

  determineAccessArights() {
    this.viewAccess = this.authService.getAccessRights("userRoleView");
    this.editAccess = this.authService.getAccessRights("userRoleEdit");
    this.addAccess = this.authService.getAccessRights("userRoleAdd");
  }

  onSearch() {
    let request = new UserTypeRef;
    this.data = [];
    this.filterData = null;
    this.admin.findMemberType(request)
      .pipe(first())
      .subscribe(
        response => {
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.data = response.data;
          this.filterData = response.data;
        },
        error => {
          this.alertService.displayError(error);
        });
  }

  onEdit() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to modify');
      return;
    }
    this.storage.setEntity(this.selectedData);
    this.router.navigate(['/system-admin/user-role', 'Edit']);
  }

  onView() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to view');
      return;
    }
    this.storage.setEntity(this.selectedData);
    this.router.navigate(['/system-admin/user-role', 'View']);
  }

  onAdd() {
    this.router.navigate(["/system-admin/user-role", "Add"]);
  }

}
