import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ChargeTierData } from 'app/shared/models/charge-tier-data';
import { CommissionData } from 'app/shared/models/commission-data';
import { TransCode } from 'app/shared/models/trans-code';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { RouterServiceService } from 'app/shared/services/router-service.service';
import { SecurityService } from 'app/shared/services/security.service';
import { StorageService } from 'app/shared/services/storage.service';
import { DynamicDialogRef, DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';
import { ServicesCommissionTiersComponent } from '../services-commission-tiers/services-commission-tiers.component';

@Component({
  selector: 'app-services-commission',
  templateUrl: './services-commission.component.html',
  styleUrls: ['./services-commission.component.scss']
})
export class ServicesCommissionComponent implements OnInit {
  loading = [false, false, false];
  submitted = false;
  selectedData: CommissionData;
  product: TransCode;
  isEdit = false;
  isView = false;
  isAddMode = false;
  segDataPickList = [];
  form: FormGroup;
  recordId: number;
  action: string;
  ref: DynamicDialogRef;
  constructor(
    private route: ActivatedRoute,
    private alertService: AlertService,
    private fb: FormBuilder,
    private navigation: RouterServiceService,
    private securityService: SecurityService,
    private agentBanking: AgentBankingService,
    private storageService: StorageService,
    // public ref: DynamicDialogRef,
    // public config: DynamicDialogConfig,
    public dialogService: DialogService,
  ) {

  }

  ngOnInit() {
    this.action = this.route.snapshot.params['id'];
    this.product = this.storageService.getSelectedProduct();
    this.prepareFormBasedOnAction();
    this.createForms();
    this.onSearch();
  }

  ngOnDestroy() {
    //this.storageService.closeEntity();
  }

  prepareFormBasedOnAction() {
    this.isEdit = true;
    this.selectedData = this.storageService.getEntity();
    this.isAddMode = !this.selectedData;
  }

  createForms() {
    this.form = this.fb.group({
      commissionId: [null],
      serviceId: [this.product.serviceId],
      commissionType: [null],
      amount: [null],
      calculationBasis: [null],
      status: ["Active"],
      createdBy: [this.securityService.currentUser.userName],
      createDate: [this.securityService.currentUser.processDate],
      modifiedBy: [this.securityService.currentUser.userName],
      modifyDate: [this.securityService.currentUser.processDate],
    });
  }

  get f() {
    return this.form.controls;
  }


  validation_messages = {

  };


  onSearch() {
    let request = new CommissionData;
    request.serviceId = this.product.serviceId;
    this.loading[0] = true;
    this.agentBanking.findServiceCommission(request)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.selectedData = response.data;
          this.f.commissionId.setValue(this.selectedData.commissionId);
          this.f.serviceId.setValue(this.selectedData.serviceId);
          this.f.commissionType.setValue(this.selectedData.commissionType.trim());
          this.f.amount.setValue(this.selectedData.amount);
          this.f.calculationBasis.setValue(this.selectedData.calculationBasis);
          this.f.status.setValue(this.selectedData.status);
          this.f.createdBy.setValue(this.selectedData.createdBy);
          this.f.createDate.setValue(this.selectedData.createDate);
        },
        error => {
          this.loading[0] = false;
          this.alertService.displayError(error);
        });
  }

  async openTierWindow() {
    this.ref = this.dialogService.open(ServicesCommissionTiersComponent, {
      header: 'Commission Tier Information',
      width: '45%',
      closable: true,
      styleClass: "p-dialog-titlebar",
      contentStyle: { "max-height": "500px", "overflow": "auto" },
      ////baseZIndex: 10000,,
      data: this.selectedData,
    });

    this.ref.onClose.subscribe((product: ChargeTierData) => {
      if (product) {

      } else {
        // this.chartOfAcctData = null;
        // this.newGLChart = null;
        // this.chartOfAcctData = this.storedChartOfAcctData;
      }
    });
  }


  onSubmit() {
    this.submitted = true;
    if (this.form.invalid) {
      this.alertService.displayInfo("Please fill all the fields");
      this.loading[0] = false;
      return;
    }
    this.loading[0] = true;
    this.agentBanking.maintainServiceCommission(this.prepareSubmitData())
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
          console.log(JSON.stringify(error));
          this.loading[0] = false;
          this.submitted = false;
          this.alertService.displayError('Error ' + error.status + ": " + error.error.error);
        });
  }

  prepareSubmitData(): CommissionData {
    let request = new CommissionData;
    request.commissionId = this.f.commissionId.value;
    request.serviceId = this.f.serviceId.value;
    request.commissionType = this.f.commissionType.value;
    request.amount = this.f.amount.value;
    request.status = this.f.status.value;
    request.calculationBasis = this.f.calculationBasis.value;
    request.createdBy = this.f.createdBy.value;
    request.createDate = this.f.createDate.value;
    request.modifiedBy = this.f.modifiedBy.value;
    request.modifyDate = this.f.modifyDate.value;
    return request;
  }

  onClose() {
    this.navigation.goBack();
  }

}
