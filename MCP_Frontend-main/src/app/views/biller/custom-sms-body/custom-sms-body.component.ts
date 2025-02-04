import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { DepositProduct } from 'app/shared/models/deposit-product';
import { RouterServiceService } from 'app/shared/services/router-service.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-custom-sms-body',
  templateUrl: './custom-sms-body.component.html',
  styleUrls: ['./custom-sms-body.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class CustomSmsBodyComponent implements OnInit {
  loading = [false, false, false];
  submitted = false;
  selectedData: DepositProduct;
  customerClassId: number;
  customerType: string;
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
    private storageService: StorageService,
  ) {

  }


  ngOnInit() {
    this.action = this.route.snapshot.params['id'];
    this.customerClassId = this.route.snapshot.params['id2'];
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
      id: [null],
      prodCode: [null, Validators.required],
      description: [null, Validators.required],
      currencyId: [null, Validators.required],
      acctNoSchemeId: [null, Validators.required],
      minimumAge: [null],
      maximumAge: [null],
      gender: [null],
      bearingMethod: [null, Validators.required],
      allowMultipleAccount: [null],
      keepDailyAccruedInterest: [null],
      interestBearing: [null],
      interestBearingFrequencyValue: [null, Validators.required],
      interestBearingFrequencyCode: [null, Validators.required],
      maximumAccountPerProduct: [null],
      minimumOpeningDeposit: [null],
      minimumDepositAmount: [null],
      minimumWithdrawalAmount: [null],
      accountCycleValue: [null, Validators.required],
      accountCycleCode: [null, Validators.required],
      chargeCycleValue: [null, Validators.required],
      chargeCycleCode: [null, Validators.required],
      riskId: [null],
      minimumAccountBalance: [null],
      allowOverdraw: [null],
      dormancyPeriodCode: [null],
      dormancyPeriodValue: [null],
      freeWithdrawalAllowed: [null],
      withdrawPeriod: [null],
      withdrawValue: [null],
      maturityPeriodCode: [null],
      maturityPeriodValue: [null],
      maturityInstruction: [null],
      //createdBy: [this.adminEndPoint.currentUser.userName],
      status: ["Inactive"],
      //modifyDate: [this.adminEndPoint.currentUser.processDate],
      //createDate: [this.adminEndPoint.currentUser.processDate],
      // modifiedBy: [this.adminEndPoint.currentUser.userName],
      edit: false
    });
  }

  get f() {
    return this.form.controls;
  }

  validation_messages = {
    prodCode: [{ type: 'required', message: 'Product code is required' }],
    description: [{ type: 'required', message: 'Description is required' }],
    bearingMethod: [{ type: 'required', message: 'Bearing Method is required' }],
    accountCycleCode: [{ type: 'required', message: 'Period is required' }],
    chargeCycleCode: [{ type: 'required', message: 'Period is required' }],
    accountCycleValue: [{ type: 'required', message: 'Term is required' }],
    chargeCycleValue: [{ type: 'required', message: 'Term status is required' }],
    currencyId: [{ type: 'required', message: 'Currency is required' }],
    interestBearingFrequencyCode: [{ type: 'required', message: 'Period is required' }],
    interestBearingFrequencyValue: [{ type: 'required', message: 'Term is required' }],
    minimumAge: [{ type: 'required', message: 'Customer Type is required' }],
    maximumAccountPerProduct: [{ type: 'required', message: 'Title is required' }],
    minimumOpeningDeposit: [{ type: 'required', message: 'First name is required' }],
    minimumDepositAmount: [{ type: 'required', message: 'Last name is required' }],
    minimumWithdrawalAmount: [{ type: 'required', message: 'Birth date is required' }],
    minimumAccountBalance: [{ type: 'required', message: 'RSM is required' }],
    countryId: [{ type: 'required', message: 'Country is required' }],
    riskId: [{ type: 'required', message: 'Risk Id is required' }],
    city: [{ type: 'required', message: 'City is required' }],
    residenceFlag: [{ type: 'required', message: 'Residence flag is required' }],
    marketInfo: [{ type: 'required', message: 'Marketing campaign is required' }],
    status: [{ type: 'required', message: 'Status is required' }],
  };


  findEntityData() {
    this.f.id.setValue(this.selectedData.id);
    this.f.prodCode.setValue(this.selectedData.prodCode);
    this.f.description.setValue(this.selectedData.description);
    this.f.currencyId.setValue(this.selectedData.currencyId);
    this.f.minimumAge.setValue(this.selectedData.minimumAge);
    this.f.maximumAge.setValue(this.selectedData.maximumAge);
    this.f.acctNoSchemeId.setValue(this.selectedData.acctNoSchemeId);
    this.f.gender.setValue(this.selectedData.gender);
    this.f.bearingMethod.setValue(this.selectedData.bearingMethod);
    this.f.allowMultipleAccount.setValue(this.selectedData.allowMultipleAccount == "Y" ? true : false);
    this.f.keepDailyAccruedInterest.setValue(this.selectedData.keepDailyAccruedInterest == "Y" ? true : false);
    this.f.interestBearing.setValue(this.selectedData.interestBearing == "Y" ? true : false);
    this.f.interestBearingFrequencyValue.setValue(this.selectedData.interestBearingFrequencyValue);
    this.f.interestBearingFrequencyCode.setValue(this.selectedData.interestBearingFrequencyCode);
    this.f.maximumAccountPerProduct.setValue(this.selectedData.maximumAccountPerProduct);
    this.f.minimumOpeningDeposit.setValue(this.selectedData.minimumOpeningDeposit);
    this.f.minimumDepositAmount.setValue(this.selectedData.minimumDepositAmount);
    this.f.minimumWithdrawalAmount.setValue(this.selectedData.minimumWithdrawalAmount);
    this.f.accountCycleValue.setValue(this.selectedData.accountCycleValue);
    this.f.accountCycleCode.setValue(this.selectedData.accountCycleCode);
    this.f.chargeCycleValue.setValue(this.selectedData.chargeCycleValue);
    this.f.chargeCycleCode.setValue(this.selectedData.chargeCycleCode);
    this.f.riskId.setValue(this.selectedData.riskId);
    this.f.minimumAccountBalance.setValue(this.selectedData.minimumAccountBalance);
    this.f.allowOverdraw.setValue(this.selectedData.allowOverdraw);
    this.f.dormancyPeriodCode.setValue(this.selectedData.dormancyPeriodCode);
    this.f.dormancyPeriodValue.setValue(this.selectedData.dormancyPeriodValue);
    this.f.freeWithdrawalAllowed.setValue(this.selectedData.freeWithdrawalAllowed);
    this.f.withdrawPeriod.setValue(this.selectedData.withdrawPeriod);
    this.f.withdrawValue.setValue(this.selectedData.withdrawValue);
    this.f.maturityPeriodCode.setValue(this.selectedData.maturityPeriodCode);
    this.f.maturityPeriodValue.setValue(this.selectedData.maturityPeriodValue);
    this.f.maturityInstruction.setValue(this.selectedData.maturityInstruction);
    this.f.createdBy.setValue(this.selectedData.createdBy);
    this.f.status.setValue(this.selectedData.status);
    this.f.createDate.setValue(this.selectedData.createDate);
    this.f.edit.setValue(true);
  }


  onSubmit() {
    // this.submitted = true;
    // if (this.form.invalid) {
    //   this.alertService.displayInfo("Please fill all the fields");
    //   this.loading[0] = false;
    //   return;
    // }
    // this.loading[0] = true;
    // this.depositService.maintainDepositProduct(this.prepareSubmitData())
    //   .pipe(first())
    //   .subscribe(
    //     response => {
    //       this.loading[0] = false;
    //       if (response.code !== "00") {
    //         this.alertService.displayError(response.message);
    //         return;
    //       }
    //       this.alertService.displaySuccess("Record Saved Successfully");
    //       this.submitted = false;
    //       this.onClose();
    //     },
    //     error => {
    //       this.loading[0] = false;
    //       this.submitted = false;
    //       this.alertService.displayError(error);
    //     });
  }


  prepareSubmitData(): DepositProduct {
    let request = new DepositProduct;
    request.id = this.f.id.value;
    request.prodCode = this.f.prodCode.value;
    request.description = this.f.description.value;
    request.currencyId = this.f.currencyId.value;
    request.minimumAge = this.f.minimumAge.value;
    request.maximumAge = this.f.maximumAge.value;
    request.acctNoSchemeId = this.f.acctNoSchemeId.value;
    request.gender = this.f.gender.value;
    request.bearingMethod = this.f.bearingMethod.value;
    request.allowMultipleAccount = this.f.allowMultipleAccount.value == true ? "Y" : "N";
    request.keepDailyAccruedInterest = this.f.keepDailyAccruedInterest.value == true ? "Y" : "N";
    request.interestBearing = this.f.interestBearing.value == true ? "Y" : "N";
    request.interestBearingFrequencyValue = this.f.interestBearingFrequencyValue.value;
    request.interestBearingFrequencyCode = this.f.interestBearingFrequencyCode.value;
    request.maximumAccountPerProduct = this.f.maximumAccountPerProduct.value;
    request.minimumOpeningDeposit = this.f.minimumOpeningDeposit.value;
    request.minimumDepositAmount = this.f.minimumDepositAmount.value;
    request.minimumWithdrawalAmount = this.f.minimumWithdrawalAmount.value;
    request.accountCycleValue = this.f.accountCycleValue.value;
    request.accountCycleCode = this.f.accountCycleCode.value;
    request.chargeCycleValue = this.f.chargeCycleValue.value;
    request.chargeCycleCode = this.f.chargeCycleCode.value;
    request.riskId = this.f.riskId.value;
    request.minimumAccountBalance = this.f.minimumAccountBalance.value;
    request.allowOverdraw = this.f.allowOverdraw.value;
    request.dormancyPeriodCode = this.f.dormancyPeriodCode.value;
    request.dormancyPeriodValue = this.f.dormancyPeriodValue.value;
    request.freeWithdrawalAllowed = this.f.freeWithdrawalAllowed.value;
    request.withdrawPeriod = this.f.withdrawPeriod.value;
    request.withdrawValue = this.f.withdrawValue.value;
    request.maturityPeriodCode = this.f.maturityPeriodCode.value;
    request.maturityPeriodValue = this.f.maturityPeriodValue.value;
    request.maturityInstruction = this.f.maturityInstruction.value;
    request.createdBy = this.f.createdBy.value;
    request.status = this.f.status.value;
    request.createDate = this.f.createDate.value;
    request.createDate = this.f.createDate.value;
    request.edit = this.isEdit;
    return request;
  }

  onClose() {
    this.navigation.goBack();
  }
}
