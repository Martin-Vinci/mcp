import { Component, OnInit, ViewChild } from "@angular/core";
import { FormGroup, FormBuilder } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { first } from "rxjs/operators";
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ColumnMode, DatatableComponent, SelectionType } from "@swimlane/ngx-datatable";
import { UserRoleData } from "app/shared/models/user-role-data";
import { UserTypeRef } from "app/shared/models/user-type";
import { MenuAccessRight } from "app/shared/models/menu-access-right";
import { AlertService } from "app/shared/services/alert.service";
import { NavigationService } from "app/shared/services/navigation.service";
import { SecurityService } from "app/shared/services/security.service";
import { StorageService } from "app/shared/services/storage.service";
import { SystemAdminService } from "app/shared/services/system-admin.service";
import { UserTypeAccessRight } from "app/shared/models/user-type-access";
import { RouterServiceService } from "app/shared/services/router-service.service";


@Component({
  selector: 'app-user-role-access-right',
  templateUrl: './user-role-access-right.component.html',
  styleUrls: ['./user-role-access-right.component.scss']
})
export class UserRoleAccessRightComponent implements OnInit {
  @ViewChild(DatatableComponent) table: DatatableComponent;

  loading = [false, false, false];
  submitted = false;
  selectedData: UserTypeRef;
  public limitRef = 10;
  unAssignedAccess: MenuAccessRight[];
  selectedUnAssignedAccess = [];

  assignedOperationService: MenuAccessRight[];
  selectedAssignedService = [];

  isEdit = false;
  isView = false;
  isAddMode = false;
  loadingIndicator: boolean = true;
  cssBodyWidth: string = "col-10";
  form: FormGroup;
  action: string;
  ref: DynamicDialogRef;
  SelectionType = SelectionType;
  public ColumnMode = ColumnMode;
  public columns = [
    { name: "Access Right", prop: "description" },
  ];
  private filterDataUnAssigned = [];
  private filterDataAssigned = [];
  constructor(
    private route: ActivatedRoute,
    private alertService: AlertService,
    private fb: FormBuilder,
    private navigation: RouterServiceService,
    private authService: SecurityService,
    private storageService: StorageService,
    private adminService: SystemAdminService,
    public dialogService: DialogService,
  ) {

  }


  ngOnInit() {
    this.action = this.route.snapshot.params['id'];
    this.selectedData = this.storageService.getEntity();
    this.createForms();
    this.findUnAssignedAccess();
    this.findAssignedOperationServices();
  }


  ngOnDestroy() {
    //this.storageService.closeEntity();
  }

  get f() {
    return this.form.controls;
  }

  createForms() {
    this.form = this.fb.group({
      id: [null],
      edit: false
    });
  }


  findAssignedOperationServices() {
    let request = new UserTypeAccessRight;
    request.userTypeId = this.selectedData.userTypeId;
    request.accessCategory = "GLOBAL";
    this.loading[0] = true;
    this.assignedOperationService = [];
    this.filterDataAssigned = null;
    this.adminService.findAssignedAccessRights(request)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          if (response.code !== "00") {
            return;
          }
          this.assignedOperationService = response.data;
          this.filterDataAssigned = response.data;
        },
        error => {
          this.loading[0] = false;
          this.alertService.displayError(error);
        });
  }


  findUnAssignedAccess() {
    let request = new UserTypeAccessRight;
    request.userTypeId = this.selectedData.userTypeId;
    request.accessCategory = "GLOBAL";
    this.unAssignedAccess = null;
    this.loading[0] = true;
    this.unAssignedAccess = [];
    this.filterDataUnAssigned = null;
    this.adminService.findUnAssignedAccessRights(request)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          if (response.code !== "00") {
            return;
          }
          this.unAssignedAccess = response.data;
          this.filterDataUnAssigned = response.data;
        },
        error => {
          this.loading[0] = false;
          this.alertService.displayError(error);
        });
  }


  assignOperationService() {
    if (this.selectedUnAssignedAccess.length == 0) {
      this.alertService.displayError('Select access rights to assign');
      return;
    }

    let records: UserTypeAccessRight[] = [];
    let request = new UserTypeAccessRight
    for (const element of this.selectedUnAssignedAccess) {
      request = new UserTypeAccessRight;
      request.userTypeId = this.selectedData.userTypeId;
      request.menuId = element.menuId;
      request.createdBy = this.authService.currentUser.userName;
      records.push(request);
    }
    this.loading[1] = true;
    this.adminService.assignAccessRight(records)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[1] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.selectedUnAssignedAccess = [];
          this.findAssignedOperationServices();
          this.findUnAssignedAccess();
        },
        error => {
          this.loading[1] = false;
          this.alertService.displayError(error);
        });
  }


  revokeOperationService() {
    if (this.selectedAssignedService.length == 0) {
      this.alertService.displayError('Select access rights to revoke');
      return;
    }

    let records: UserTypeAccessRight[] = [];
    let request = new UserTypeAccessRight
    for (const element of this.selectedAssignedService) {
      request = new UserTypeAccessRight;
      request.userTypeId = this.selectedData.userTypeId;
      request.menuId = element.menuId;
      request.createdBy = this.authService.currentUser.userName;
      records.push(request);
    }
    this.loading[2] = true;
    this.adminService.revokeAccessRight(records)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[2] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.selectedAssignedService = [];
          this.findAssignedOperationServices();
          this.findUnAssignedAccess();
        },
        error => {
          this.loading[1] = false;
          this.alertService.displayError(error);
        });
  }


  filterUpdateUnAssigned(event) {
    const val = event.target.value.toLowerCase();
    // filter our data
    const temp = this.filterDataUnAssigned.filter(function (d) {
      return d.description.toLowerCase().indexOf(val) !== -1 || !val;
    });
    // update the rows
    this.unAssignedAccess = temp;
    // Whenever the filter changes, always go back to the first page
    this.table.offset = 0;
  }

  filterUpdateAssigned(event) {
    const val = event.target.value.toLowerCase();
    // filter our data
    const temp = this.filterDataAssigned.filter(function (d) {
      return d.description.toLowerCase().indexOf(val) !== -1 || !val;
    });
    // update the rows
    this.assignedOperationService = temp;
    // Whenever the filter changes, always go back to the first page
    this.table.offset = 0;
  }


  unAssignedAccessOnSelect({ selected }) {
    this.selectedUnAssignedAccess.splice(0, this.selectedUnAssignedAccess.length);
    this.selectedUnAssignedAccess.push(...selected);
    console.log(this.selectedUnAssignedAccess);
  }
  assignedAccessOnSelect({ selected }) {
    this.selectedAssignedService.splice(0, this.selectedAssignedService.length);
    this.selectedAssignedService.push(...selected);
    console.log(this.selectedAssignedService);
  }


  onClose() {
    this.navigation.goBack();
  }
}
