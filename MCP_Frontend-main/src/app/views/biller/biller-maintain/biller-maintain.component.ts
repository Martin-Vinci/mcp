import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Biller } from 'app/shared/models/biller';
import { AlertService } from 'app/shared/services/alert.service';
import { BillerService } from 'app/shared/services/biller.service';
import { RouterServiceService } from 'app/shared/services/router-service.service';
import { SecurityService } from 'app/shared/services/security.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MenuItem } from 'primeng/api';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-biller-maintain',
  templateUrl: './biller-maintain.component.html',
  styleUrls: ['./biller-maintain.component.scss']
})
export class BillerMaintainComponent implements OnInit {
  loading = [false, false, false];
  submitted = false;
  selectedData: Biller;
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
    private systemAdmin: BillerService
  ) {

  }

  ngOnInit() {
    this.action = this.route.snapshot.params['id'];
    this.initializeSubMenus();
    this.createForms();
    this.prepareFormBasedOnAction();
    if (this.isAddMode == false) {
      this.findEntityData();
    }
  }

  ngOnDestroy() {
    //this.storageService.closeEntity();
  }


  initializeSubMenus() {
    this.items = [{
      label: 'Menu',
      items: [{
        label: 'Product Category',
        icon: 'pi pi-briefcase',
        routerLink: '/biller-control/biller-category-search',
      },
      {
        label: 'Products',
        icon: 'pi pi-pencil',
        routerLink: '/biller-control/biller-product-search',
      },
      ]
    }
    ];
  }


  prepareFormBasedOnAction() {
    if (this.action == "Add") {
      this.cssBodyWidth = "col-12";
      this.isAddMode = !this.selectedData;
    }
    else if (this.action == "View") {
      this.isView = true;
      this.selectedData = this.storageService.getSelectedProduct();
      this.isAddMode = !this.selectedData;
    }
    else if (this.action == "Edit") {
      this.isEdit = true;
      this.selectedData = this.storageService.getSelectedProduct();
      this.isAddMode = !this.selectedData;
    }
  }

  createForms() {
    this.form = this.fb.group({
      id: [null],
      billerCode: [null],
      description: [null],
      acctNo: [null],
      vendorCode: [null],
      vendorPassword: [null],
      smsTemplate: [null],
      status: ["ACTIVE"],
      createdBy: [this.securityService.currentUser.userName],
      createDate: [this.securityService.currentUser.processDate],
      modifyBy: [this.securityService.currentUser.userName],
      modifyDt: [this.securityService.currentUser.processDate],
      edit: false
    });
  }

  
  get f() {
    return this.form.controls;
  }

  validation_messages = {
   
  };


  findEntityData() {
    this.f.id.setValue(this.selectedData.id);
    this.f.billerCode.setValue(this.selectedData.billerCode);
    this.f.description.setValue(this.selectedData.description);
    this.f.acctNo.setValue(this.selectedData.acctNo);
    this.f.vendorCode.setValue(this.selectedData.vendorCode);
    this.f.vendorPassword.setValue(this.selectedData.vendorPassword);
    this.f.smsTemplate.setValue(this.selectedData.smsTemplate);
    this.f.status.setValue(this.selectedData.status.toString().trim());
    this.f.createdBy.setValue(this.selectedData.createdBy);
    this.f.createDate.setValue(this.selectedData.createDate);
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
    this.systemAdmin.maintainBiller(this.prepareSubmitData())
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

  prepareSubmitData(): Biller {
    let request = new Biller;
    request.id = this.f.id.value;
    request.billerCode = this.f.billerCode.value;
    request.description = this.f.description.value;
    request.acctNo = this.f.acctNo.value;
    request.vendorCode = this.f.vendorCode.value;
    request.vendorPassword = this.f.vendorPassword.value;
    request.smsTemplate = this.f.smsTemplate.value;
    request.status = this.f.status.value;
    request.createdBy = this.f.createdBy.value;
    request.createDate = this.f.createDate.value;
    return request;
  }


  onClose() {
    this.navigation.goBack();
  }

}
