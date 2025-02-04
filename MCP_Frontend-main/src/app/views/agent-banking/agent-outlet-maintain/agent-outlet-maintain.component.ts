import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AgentData } from 'app/shared/models/agent-data';
import { MobileUser } from 'app/shared/models/mobile-user';
import { MobileUserAcct } from 'app/shared/models/mobile-user-acct';
import { OutletData } from 'app/shared/models/outlet-data';
import { ProductInterface } from 'app/shared/models/product-interface';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { NavigationService } from 'app/shared/services/navigation.service';
import { RouterServiceService } from 'app/shared/services/router-service.service';
import { SecurityService } from 'app/shared/services/security.service';
import { StorageService } from 'app/shared/services/storage.service';
import { DynamicDialogRef, DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';
import { EntityAccountModalComponent } from '../entity-account-modal/entity-account-modal.component';

@Component({
  selector: 'app-agent-outlet-maintain',
  templateUrl: './agent-outlet-maintain.component.html',
  styleUrls: ['./agent-outlet-maintain.component.scss']
})
export class AgentOutletMaintainComponent implements OnInit {
  loading = [false, false, false];
  submitted = false;
  selectedData: MobileUser;
  product: MobileUser;
  isEdit = false;
  isView = false;
  isAddMode = false;
  segDataPickList = [];
  form: FormGroup;
  recordId: number;
  action: string;
  data: MobileUserAcct[];
  selectedAccount: MobileUserAcct;
  ref: DynamicDialogRef;
  canAddAcct = false;
  constructor(
    private route: ActivatedRoute,
    private alertService: AlertService,
    private fb: FormBuilder,
    private navigation: RouterServiceService,
    private agentBankingService: AgentBankingService,
    private storageService: StorageService,
    public dialogService: DialogService,
  ) {

  }

  ngOnInit() {
    this.action = this.route.snapshot.params['id'];
    this.product = this.storageService.getSelectedProduct();
    this.prepareFormBasedOnAction();
    this.createForms();
    if (this.isAddMode == false) {
      this.findEntityData();
    }
  }

  ngOnDestroy() {
    //this.storageService.closeEntity();
  }

  
  prepareFormBasedOnAction() {
    if (this.action == "Add") {
      this.isAddMode = true;
    }
    else if (this.action == "View") {
      this.isView = true;
      this.selectedData = this.storageService.getEntity();
      this.isAddMode = !this.selectedData;
      this.getAccounts();
    }
    else if (this.action == "Edit") {
      this.isEdit = true;
      this.canAddAcct = true;
      this.selectedData = this.storageService.getEntity();
      this.isAddMode = !this.selectedData;
      this.getAccounts();
    }
  }

  createForms() {
    this.form = this.fb.group({
      id: [null],
      phoneNumber: [null],
      customerName: [null],
      dateCreated: [null],
      lockedFlag: [false],
      acctType: ["OUTLET"],
      failedLoginCount: [null],
      pinChangeFlag: [false],
      authImsi: [null],
      tinNo: [null],
      registrationNo: [null],
      authImei: [null],
      activationCode: [null],
      useAndroidChannel: [true],
      useUssdChannel: [true],
      wapOtp: [null],
      wapOtpExpiry: [null],
      approvalStatus: [true],
      createdBy: [null],
      approvedBy: [null],
      dateApproved: [null],
      birthDate: [null],
      physicalAddress: [null],
      latitude: [null],
      longitude: [null],
      postalAddress: [null],
      gender: [null],
      entityCode: [this.product.entityCode],
      outletCode: [null],
      edit: false
    });
  }
  get f() {
    return this.form.controls;
  }


  validation_messages = {
    position: [{ type: 'required', message: 'Position is required' }],
    description: [{ type: 'required', message: 'Description is required' }],
    dataTypeCode: [{ type: 'required', message: 'Category is required' }],
    segDataId: [{ type: 'required', message: 'Segment data is required' }],
  };


  getAccounts() {
    let request = new MobileUserAcct;
    //request.entityId = this.selectedData.phoneNumber;
    request.mobileUserId = this.selectedData.id;
    this.loading[1] = true;
    this.agentBankingService.findMobileUserAccounts(request)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[1] = false;
          if (response.code !== "00") {
            //this.alertService.displayError(response.message);
            return;
          }
          this.data = response.data;
          this.canAddAcct = false;
        },
        error => {
          this.loading[1] = false;
          this.alertService.displayError(error);
        });
  }

  async openAccountWindow(accountsCategory: string) {
    this.selectedData.accountsCategory = accountsCategory;
    this.ref = this.dialogService.open(EntityAccountModalComponent, {
      header: 'Entity Accounts [ ' + this.selectedData.customerName + ' ]',
      width: '40%',
      closable: true,
      styleClass: "p-dialog-titlebar",
      contentStyle: { "max-height": "500px", "overflow": "auto" },
      ////baseZIndex: 10000,,
      data: this.selectedData,
    });

    this.ref.onClose.subscribe((product: MobileUserAcct) => {
      if (product) {
        this.getAccounts();
      } else {
        // this.chartOfAcctData = null;
        // this.newGLChart = null;
        // this.chartOfAcctData = this.storedChartOfAcctData;
      }
    });
  }

  onPinReset() {
    this.submitted = true;
    if (this.form.invalid) {
      this.alertService.displayInfo("Please fill all the fields");
      return;
    }
    this.loading[2] = true;
    this.agentBankingService.pinReset(this.prepareSubmitData())
      .pipe(first())
      .subscribe(
        response => {
          this.loading[2] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.alertService.displaySuccess("Record Saved Successfully");
          this.submitted = false;
          this.onClose();
        },
        error => {
          this.loading[2] = false;
          this.submitted = false;
          this.alertService.displayError(error);
        });
  }
  prepareSubmitData(): MobileUser {
    let request = new MobileUser;
    request.id = this.f.id.value;
    request.phoneNumber = this.f.phoneNumber.value;
    request.customerName = this.f.customerName.value;
    request.dateCreated = this.f.dateCreated.value;
    request.lockedFlag = this.f.lockedFlag.value;
    request.acctType = this.f.acctType.value;
    request.failedLoginCount = this.f.failedLoginCount.value;
    request.pinChangeFlag = this.f.pinChangeFlag.value;
    request.authImsi = this.f.authImsi.value;
    request.authImei = this.f.authImei.value;
    request.activationCode = this.f.activationCode.value;
    request.useAndroidChannel = this.f.useAndroidChannel.value;
    request.useUssdChannel = this.f.useUssdChannel.value;
    request.wapOtp = this.f.wapOtp.value;
    request.wapOtpExpiry = this.f.wapOtpExpiry.value;
    request.approvalStatus = this.f.approvalStatus.value;
    request.createdBy = this.f.createdBy.value;
    request.approvedBy = this.f.approvedBy.value;
    request.dateApproved = this.f.dateApproved.value;
    request.birthDate = this.f.birthDate.value;
    request.physicalAddress = this.f.physicalAddress.value;
    request.postalAddress = this.f.postalAddress.value;
    request.gender = this.f.gender.value;
    request.entityCode = this.f.entityCode.value;
    request.outletCode = this.f.outletCode.value;
    return request;
  }

  findEntityData() {
    try {
      this.f.id.setValue(this.selectedData.id);
      this.f.phoneNumber.setValue(this.selectedData.phoneNumber);
      this.f.customerName.setValue(this.selectedData.customerName);
      this.f.dateCreated.setValue(this.selectedData.dateCreated);
      this.f.lockedFlag.setValue(this.selectedData.lockedFlag);
      this.f.acctType.setValue(this.selectedData.acctType);
      this.f.failedLoginCount.setValue(this.selectedData.failedLoginCount);
      this.f.pinChangeFlag.setValue(this.selectedData.pinChangeFlag);
      this.f.authImsi.setValue(this.selectedData.authImsi);
      this.f.authImei.setValue(this.selectedData.authImei);
      this.f.activationCode.setValue(this.selectedData.activationCode);
      this.f.useAndroidChannel.setValue(this.selectedData.useAndroidChannel);
      this.f.useUssdChannel.setValue(this.selectedData.useUssdChannel);
      this.f.wapOtp.setValue(this.selectedData.wapOtp);
      this.f.wapOtpExpiry.setValue(this.selectedData.wapOtpExpiry);
      this.f.approvalStatus.setValue(this.selectedData.approvalStatus);
      this.f.createdBy.setValue(this.selectedData.createdBy);
      this.f.approvedBy.setValue(this.selectedData.approvedBy);
      this.f.dateApproved.setValue(this.selectedData.dateApproved);
      this.f.birthDate.setValue(this.selectedData.birthDate);
      this.f.physicalAddress.setValue(this.selectedData.physicalAddress);
      this.f.postalAddress.setValue(this.selectedData.postalAddress);
      this.f.gender.setValue(this.selectedData.gender);
      this.f.entityCode.setValue(this.selectedData.entityCode);
      this.f.outletCode.setValue(this.selectedData.outletCode);
    }
    catch (e) {
      console.log(e);
    }
  }
  onSubmit() {
    this.submitted = true;
    if (this.form.invalid) {
      this.alertService.displayInfo("Please fill all the fields");
      this.loading[0] = false;
      return;
    }
    this.loading[0] = true;
    this.agentBankingService.maintainMobileUser(this.prepareSubmitData())
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

  onClose() {
    this.navigation.goBack();
  }

}
