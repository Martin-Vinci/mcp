import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { DepositProduct } from 'app/shared/models/deposit-product';
import { UserData } from 'app/shared/models/user-data';
import { UserTypeRef } from 'app/shared/models/user-type';
import { AlertService } from 'app/shared/services/alert.service';
import { RouterServiceService } from 'app/shared/services/router-service.service';
import { SecurityService } from 'app/shared/services/security.service';
import { StorageService } from 'app/shared/services/storage.service';
import { SystemAdminService } from 'app/shared/services/system-admin.service';
import { MenuItem } from 'primeng/api';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-users-maintain',
  templateUrl: './users-maintain.component.html',
  styleUrls: ['./users-maintain.component.scss']
})
export class UsersMaintainComponent implements OnInit {
  loading = [false, false, false];
  submitted = false;
  selectedData: UserData;
  customerClassId: number;
  customerType: string;
  isEdit = false;
  isView = false;
  isAddMode = false;
  cssBodyWidth: string = "col-10";
  items: MenuItem[];
  form: FormGroup;
  action: string;
  userRolesPickList = [];
  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private navigation: RouterServiceService,
    private securityService: SecurityService,
    private storageService: StorageService,
    private alertService: AlertService,
    private systemAdmin: SystemAdminService
  ) {

  }

  ngOnInit() {
    this.action = this.route.snapshot.params['id'];
    this.createForms();
    this.prepareFormBasedOnAction();
    this. findUserRole();
    if (this.isAddMode == false) {
      this.findEntityData();
    }
  }

  ngOnDestroy() {
    //this.storageService.closeEntity();
  }

  prepareFormBasedOnAction() {
    if (this.action == "Add") {
      this.cssBodyWidth = "col-12";
      if (this.customerClassId != null)
        this.f.customerClassId.setValue(parseInt(this.customerClassId.toString()));
      this.isAddMode = !this.selectedData;
    }
    else if (this.action == "View") {
      this.isView = true;
      this.selectedData = this.storageService.getEntity();
      this.isAddMode = !this.selectedData;
    }
    else if (this.action == "Edit") {
      this.isEdit = true;
      this.selectedData = this.storageService.getEntity();
      this.isAddMode = !this.selectedData;
    }
  }

  createForms() {
    this.form = this.fb.group({
      employeeId: [null],
      userName: [null],
      fullName: [null],
      userRoleId: [null],
      emailAddress: [null],
      phoneNo: [null],
      receiveBillerStmnt: [null],
      lockUser: [null],
      pwdEnhancedFlag: [null],
      userPwd: [null],
      status: ["Active"],
      createdBy: [this.securityService.currentUser.userName],
      createDt: [this.securityService.currentUser.processDate],
      modifyBy: [this.securityService.currentUser.userName],
      modifyDt: [this.securityService.currentUser.processDate],
      edit: false
    });
  }

  get f() {
    return this.form.controls;
  }


  findUserRole() {
    let dataList: UserTypeRef[] = [];
    this.userRolesPickList = [];
    let request = new UserTypeRef;
    this.systemAdmin.findMemberType(request)
      .pipe(first())
      .subscribe(
        response => {
          if (response.code !== "00")
            return;
          dataList = response.data;
          for (var i = 0; i < dataList.length; i++) {
           // if (dataList[i].status == "Active") {
              this.userRolesPickList.push(dataList[i]);
           // }
          }
        });
  }




  validation_messages = {
   
  };


  findEntityData() {
    this.f.employeeId.setValue(this.selectedData.employeeId);
    this.f.userName.setValue(this.selectedData.userName);
    this.f.fullName.setValue(this.selectedData.fullName);
    this.f.userRoleId.setValue(this.selectedData.userRoleId);
    this.f.emailAddress.setValue(this.selectedData.emailAddress);
    this.f.phoneNo.setValue(this.selectedData.phoneNo);
    this.f.receiveBillerStmnt.setValue(this.selectedData.receiveBillerStmnt);
    this.f.lockUser.setValue(this.selectedData.lockUser);
    this.f.pwdEnhancedFlag.setValue(this.selectedData.pwdEnhancedFlag);
    this.f.userPwd.setValue(this.selectedData.userPwd);
    this.f.status.setValue(this.selectedData.status);
    this.f.createdBy.setValue(this.selectedData.createdBy);
    this.f.createDt.setValue(this.selectedData.createDt);
    this.f.edit.setValue(true);
  }


  onSubmit() {
    this.submitted = true;
    if (this.form.invalid) {
      this.alertService.displayInfo("Please fill all the fields");
      this.loading[0] = false;
      return;
    }
    this.loading[0] = true;
    this.systemAdmin.maintainUsers(this.prepareSubmitData())
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.alertService.displaySuccess("Record Saved Successfully");
          this.submitted = false;
          this.onClose();
        },
        error => {
          this.loading[0] = false;
          this.submitted = false;
          this.alertService.displayError(error);
        });
  }

  onKeyUP(inPutValue: string): void {
    this.f.userName.setValue(inPutValue.toUpperCase());
  }


  prepareSubmitData(): UserData {
    let request = new UserData;
    request.employeeId = this.f.employeeId.value;
    request.userName = this.f.userName.value;
    request.fullName = this.f.fullName.value;
    request.userRoleId = this.f.userRoleId.value;
    request.emailAddress = this.f.emailAddress.value;
    request.phoneNo = this.f.phoneNo.value;
    request.receiveBillerStmnt = this.f.receiveBillerStmnt.value;
    request.lockUser = this.f.lockUser.value;
    request.pwdEnhancedFlag = this.f.pwdEnhancedFlag.value;
    request.userPwd = this.f.userPwd.value;
    request.status = this.f.status.value;
    request.createdBy = this.f.createdBy.value;
    request.createDt = this.f.createDt.value;
    request.modifyBy = this.f.modifyBy.value;
    request.modifyDt = this.f.modifyDt.value;
    request.edit = this.isEdit;
    return request;
  }

  onClose() {
    this.navigation.goBack();
  }
}
