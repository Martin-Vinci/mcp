import { Component, OnInit } from "@angular/core";
import { FormGroup, FormBuilder, Validators } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { TransCode } from "app/shared/models/trans-code";
import { AgentBankingService } from "app/shared/services/agent-banking.service";
import { AlertService } from "app/shared/services/alert.service";
import { RouterServiceService } from "app/shared/services/router-service.service";
import { SecurityService } from "app/shared/services/security.service";
import { StorageService } from "app/shared/services/storage.service";
import { MenuItem } from "primeng/api";
import { first } from "rxjs/operators";

@Component({
  selector: 'app-services-maintain',
  templateUrl: './services-maintain.component.html',
  styleUrls: ['./services-maintain.component.scss']
})
export class ServicesMaintainComponent implements OnInit {
  loading = [false, false, false];
  submitted = false;
  selectedData: TransCode;
  isEdit = false;
  isView = false;
  isAddMode = false;
  cssBodyWidth: string = "col-10";
  items: MenuItem[];
  form: FormGroup;
  action: string;
  constructor(
    private route: ActivatedRoute,
    private alertService: AlertService,
    private fb: FormBuilder,
    private navigation: RouterServiceService,
    private adminEndPoint: SecurityService,
    private storageService: StorageService,
    private agentBankingService: AgentBankingService
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

  initializeSubMenus() {
    this.items = [{
      label: 'Menu',
      items: [{
        label: 'Charge Details',
        icon: 'pi pi-briefcase',
        routerLink: '/agent-banking/service-charge/Edit',
      },
      {
        label: 'Commission Details',
        icon: 'pi pi-pencil',
        routerLink: '/agent-banking/service-commission/Edit',
      },
      {
        label: 'Posting Details',
        icon: 'pi pi-user-edit',
        routerLink: '/agent-banking/service-posting-policy',
      },
      {
        label: 'SMS Alert',
        icon: 'pi pi-user-edit',
        routerLink: '/biller-control/sms-body',
      },
      ]
    }
    ];
  }

  createForms() {
    this.form = this.fb.group({
      serviceId: [null],
      serviceCode: [null, Validators.required],
      description: [null, Validators.required],
      transType: [null, Validators.required],
      billerAcctNo: [null],
      bankIncomeAcctNo: [null, Validators.required],
      transitAcctNo: [null],
      expenseAcctNo: [null],
      debitCredit: [null, Validators.required],
      minTransAmt: [null, Validators.required],
      maxTransAmt: [null, Validators.required],
      dailyWithdrawLimit: [null],
      transLiteral: [null, Validators.required],
      billerCode: [null],
      mobileMoneyTaxPercentage: [null],
      maintainCommissionPercentage: [null],
      maintenanceCalculationBasis: [null],
      maintenanceAccount: [null],
      status: [null, Validators.required],
      createDate: [this.adminEndPoint.currentUser.processDate],
      createdBy: [this.adminEndPoint.currentUser.userName],
      modifyDate: [this.adminEndPoint.currentUser.processDate],
      modifiedBy: [this.adminEndPoint.currentUser.userName],
      edit: false
    });
  }
 
  get f() {
    return this.form.controls;
  }

  validation_messages = {
    serviceCode: [{ type: 'required', message: 'Service code is required' }],
    description: [{ type: 'required', message: 'Description is required' }],
    transType: [{ type: 'required', message: 'Type is required' }],
    bankIncomeAcctNo: [{ type: 'required', message: 'Income account is required' }],
    debitCredit: [{ type: 'required', message: 'DRCR is required' }],
    minTransAmt: [{ type: 'required', message: 'Min trans amount is required' }],
    maxTransAmt: [{ type: 'required', message: 'Max trans amount is required' }],
    transLiteral: [{ type: 'required', message: 'Literal is required' }],
    status: [{ type: 'required', message: 'Status is required' }],
  };


  findEntityData() {
    this.f.serviceId.setValue(this.selectedData.serviceId);
    this.f.serviceCode.setValue(this.selectedData.serviceCode);
    this.f.description.setValue(this.selectedData.description);
    this.f.transType.setValue(this.selectedData.transType.trim());
    this.f.billerAcctNo.setValue(this.selectedData.billerAcctNo);
    this.f.bankIncomeAcctNo.setValue(this.selectedData.bankIncomeAcctNo);
    this.f.transitAcctNo.setValue(this.selectedData.transitAcctNo);
    this.f.expenseAcctNo.setValue(this.selectedData.expenseAcctNo);
    this.f.debitCredit.setValue(this.selectedData.debitCredit);
    this.f.minTransAmt.setValue(this.selectedData.minTransAmt);
    this.f.maxTransAmt.setValue(this.selectedData.maxTransAmt);
    this.f.dailyWithdrawLimit.setValue(this.selectedData.dailyWithdrawLimit);
    this.f.transLiteral.setValue(this.selectedData.transLiteral);
    this.f.billerCode.setValue(this.selectedData.billerCode);
    this.f.mobileMoneyTaxPercentage.setValue(this.selectedData.mobileMoneyTaxPercentage);
    this.f.maintainCommissionPercentage.setValue(this.selectedData.maintainCommissionPercentage);
    this.f.maintenanceCalculationBasis.setValue(this.selectedData.maintenanceCalculationBasis);
    this.f.maintenanceAccount.setValue(this.selectedData.maintenanceAccount);
    this.f.status.setValue(this.selectedData.status);
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
    this.agentBankingService.maintainServices(this.prepareSubmitData())
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


  prepareSubmitData(): TransCode {
    let request = new TransCode;
    request.serviceId = this.f.serviceId.value;
    request.serviceCode = this.f.serviceCode.value;
    request.description = this.f.description.value;
    request.transType = this.f.transType.value;
    request.billerAcctNo = this.f.billerAcctNo.value;
    request.bankIncomeAcctNo = this.f.bankIncomeAcctNo.value;
    request.transitAcctNo = this.f.transitAcctNo.value;
    request.expenseAcctNo = this.f.expenseAcctNo.value;
    request.debitCredit = this.f.debitCredit.value;
    request.minTransAmt = this.f.minTransAmt.value;
    request.maxTransAmt = this.f.maxTransAmt.value;
    request.dailyWithdrawLimit = this.f.dailyWithdrawLimit.value;
    request.transLiteral = this.f.transLiteral.value;
    request.billerCode = this.f.billerCode.value;
    request.mobileMoneyTaxPercentage = this.f.mobileMoneyTaxPercentage.value;
    request.maintainCommissionPercentage = this.f.maintainCommissionPercentage.value;
    request.maintenanceCalculationBasis = this.f.maintenanceCalculationBasis.value;
    request.maintenanceAccount = this.f.maintenanceAccount.value;
    request.status = this.f.status.value;
    request.createdBy = this.f.createdBy.value;
    request.createDate = this.f.createDate.value;
    request.modifyDate = this.f.modifyDate.value;
    request.modifiedBy = this.f.modifiedBy.value;
    request.edit = this.isEdit;
    return request;
  }

  onClose() {
    this.navigation.goBack();
  }

}
