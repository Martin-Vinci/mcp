import { Component, OnInit } from "@angular/core";
import { FormGroup, FormBuilder, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { ControlParameter } from "app/shared/models/control-parameter";
import { DepositProduct } from "app/shared/models/deposit-product";
import { AlertService } from "app/shared/services/alert.service";
import { RouterServiceService } from "app/shared/services/router-service.service";
import { SecurityService } from "app/shared/services/security.service";
import { StorageService } from "app/shared/services/storage.service";
import { SystemAdminService } from "app/shared/services/system-admin.service";
import { NgxUiLoaderService } from "ngx-ui-loader";
import { MenuItem } from "primeng/api";
import { first } from "rxjs/operators";

@Component({
  selector: 'app-system-parameter-maintain',
  templateUrl: './system-parameter-maintain.component.html',
  styleUrls: ['./system-parameter-maintain.component.scss']
})
export class SystemParameterMaintainComponent implements OnInit {
  loading = [false, false, false];
  submitted = false;
  selectedData: ControlParameter;
  isEdit = false;
  isView = false;
  isAddMode = false;
  cssBodyWidth: string = "col-10";
  items: MenuItem[];
  form: FormGroup;
  action: string;
  accountStructure = [];
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
      paramId: [null],
      paramCode: [null],
      paramValue: [null],
      paramDescr: [null],
      status: ["Active"], 
      userName:  [this.securityService.currentUser.userName],
      editable: ["N"],
      modifyDate:  [this.securityService.currentUser.processDate],
    });
  }

  
  get f() {
    return this.form.controls;
  }

  validation_messages = {
   
  };


  findEntityData() {
    this.f.paramId.setValue(this.selectedData.paramId);
    this.f.paramCode.setValue(this.selectedData.paramCode);
    this.f.paramValue.setValue(this.selectedData.paramValue);
    this.f.paramDescr.setValue(this.selectedData.paramDescr);
    this.f.status.setValue(this.selectedData.status);
    this.f.userName.setValue(this.selectedData.userName);
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
    this.systemAdmin.maintainParameters(this.prepareSubmitData())
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


  prepareSubmitData(): ControlParameter {
    let request = new ControlParameter;
    request.paramId = this.f.paramId.value;
    request.paramCode = this.f.paramCode.value;
    request.paramValue = this.f.paramValue.value;
    request.paramDescr = this.f.paramDescr.value;
    request.status = this.f.status.value;
    request.userName = this.f.userName.value;
    request.modifyDate = this.f.modifyDate.value;
    return request;
  }

  onClose() {
    this.navigation.goBack();
  }

}
