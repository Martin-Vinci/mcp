import { Component, OnInit } from "@angular/core";
import { FormGroup, FormBuilder, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { UserTypeRef } from "app/shared/models/user-type";
import { AlertService } from "app/shared/services/alert.service";
import { NavigationService } from "app/shared/services/navigation.service";
import { RouterServiceService } from "app/shared/services/router-service.service";
import { SecurityService } from "app/shared/services/security.service";
import { StorageService } from "app/shared/services/storage.service";
import { SystemAdminService } from "app/shared/services/system-admin.service";
import { DialogService } from "primeng/dynamicdialog";
import { first } from "rxjs/operators";



@Component({
  selector: 'app-user-roles-maintain',
  templateUrl: './user-roles-maintain.component.html',
  styleUrls: ['./user-roles-maintain.component.scss']
})
export class UserRolesMaintainComponent implements OnInit {
  loading = [false, false, false];
  submitted = false;
  selectedData: UserTypeRef;
  isEdit = false;
  isView = false;
  isAddMode = false;
  cssBodyWidth: string = "col-10";
  form: FormGroup;
  action: string;
  constructor(
    private route: ActivatedRoute,
    private router: Router,
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
    this.createForms();
    this.prepareFormBasedOnAction();
  }

  ngOnDestroy() {
    //this.storageService.closeEntity();
  }

  prepareFormBasedOnAction() {
    if (this.action == "Add") {
      this.cssBodyWidth = "col-12";
      this.isAddMode = !this.selectedData;
    }
    else if (this.action == "View") {
      this.isView = true;
      this.selectedData = this.storageService.getEntity();
      this.isAddMode = !this.selectedData;
      this.findEntityData();
    }

    else if (this.action == "Edit") {
      this.isEdit = true;
      this.selectedData = this.storageService.getEntity();
      this.isAddMode = !this.selectedData;
      this.findEntityData();
    }
  }

  createForms() {
    this.form = this.fb.group({
      userTypeId: [null],
      category: ['BACKOFFICE'],
      description:[null, Validators.required],
      createdBy: [this.authService.currentUser.userName],
      createDt: [this.authService.currentUser.processDate],
      modifiedBy: [this.authService.currentUser.userName],
      modifyDt: [this.authService.currentUser.processDate],
      edit: false
    });
  }

  get f() {
    return this.form.controls;
  }

  validation_messages = {
    estateName: [{ type: 'required', message: 'Service code is required' }],
    estateLocation: [{ type: 'required', message: 'Description is required' }],
  };


  findEntityData() {
    this.f.userTypeId.setValue(this.selectedData.userTypeId);
    this.f.description.setValue(this.selectedData.description);
    this.f.createdBy.setValue(this.selectedData.createdBy);
    this.f.createDt.setValue(this.selectedData.createDt);
  }
  onSubmit() {
    this.submitted = true;
    if (this.form.invalid) {
      this.alertService.displayInfo("Data validation failed. Fill all required fields correctly");
      return;
    }

    this.loading[0] = true;
    this.adminService.maintainMemberType(this.prepareSubmitData())
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          // this.alertService.displaySuccess("Record Saved Successfully");
          this.submitted = false;
          this.onClose();
        },
        error => {
          this.submitted = false;
          this.alertService.displayError(error);
        });
  }

  prepareSubmitData(): UserTypeRef {
    let request = new UserTypeRef;
    request.userTypeId = this.f.userTypeId.value;
    request.description = this.f.description.value;
    request.createdBy = this.f.createdBy.value;
    request.modifiedBy = this.f.modifiedBy.value;
    return request;
  }
  onClose() {
    this.navigation.goBack();
  }

  
  goToAccessRights() {
    this.router.navigate(['/system-admin/user-role-access']);
  }

}
