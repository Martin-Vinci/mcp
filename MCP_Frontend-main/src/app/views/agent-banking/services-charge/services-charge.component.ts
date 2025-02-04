import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ChargeData } from 'app/shared/models/charge-data';
import { ChargeTierData } from 'app/shared/models/charge-tier-data';
import { TransCode } from 'app/shared/models/trans-code';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { RouterServiceService } from 'app/shared/services/router-service.service';
import { SecurityService } from 'app/shared/services/security.service';
import { StorageService } from 'app/shared/services/storage.service';
import { DynamicDialogRef, DynamicDialogConfig, DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';
import { ServicesChargeTiersComponent } from '../services-charge-tiers/services-charge-tiers.component';

@Component({
  selector: 'app-services-charge',
  templateUrl: './services-charge.component.html',
  styleUrls: ['./services-charge.component.scss']
})
export class ServicesChargeComponent implements OnInit {
  loading = [false, false, false];
  submitted = false;
  selectedData: ChargeData;
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
      chargeId: [null],
      serviceId: [this.product.serviceId],
      chargeType: [null],
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
    let request = new ChargeData;
    request.serviceId = this.product.serviceId;
    this.loading[0] = true;
    this.agentBanking.findServiceCharge(request)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.selectedData = response.data;
          this.f.chargeId.setValue(this.selectedData.chargeId);
          this.f.serviceId.setValue(this.selectedData.serviceId);
          this.f.chargeType.setValue(this.selectedData.chargeType.trim());
          this.f.amount.setValue(this.selectedData.amount);
          this.f.status.setValue(this.selectedData.status);
          this.f.calculationBasis.setValue(this.selectedData.calculationBasis);
          this.f.createdBy.setValue(this.selectedData.createdBy);
          this.f.createDate.setValue(this.selectedData.createDate);
        },
        error => {
          this.loading[0] = false;
          this.alertService.displayError(error);
        });
  }

  async openTierWindow() {
    this.ref = this.dialogService.open(ServicesChargeTiersComponent, {
      header: 'Charge Tier Information',
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
    this.agentBanking.maintainServiceCharge(this.prepareSubmitData())
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

  prepareSubmitData(): ChargeData {
    let request = new ChargeData;
    request.chargeId = this.f.chargeId.value;
    request.serviceId = this.f.serviceId.value;
    request.chargeType = this.f.chargeType.value;
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
