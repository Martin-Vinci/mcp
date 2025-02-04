import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { OutletData } from 'app/shared/models/outlet-data';
import { PostingPolicyItem, ServicePostingPolicy } from 'app/shared/models/service-posting-policy';
import { TransCode } from 'app/shared/models/trans-code';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { RouterServiceService } from 'app/shared/services/router-service.service';
import { SecurityService } from 'app/shared/services/security.service';
import { StorageService } from 'app/shared/services/storage.service';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-services-posting-maintain',
  templateUrl: './services-posting-maintain.component.html',
  styleUrls: ['./services-posting-maintain.component.scss']
})
export class ServicesPostingMaintainComponent implements OnInit {
  loading = [false, false, false];
  submitted = false;
  selectedData: ServicePostingPolicy;
  accountTypes: PostingPolicyItem[];
  amountTypes: PostingPolicyItem[];
  product: TransCode;
  isEdit = false;
  isView = false;
  isAddMode = false;
  segDataPickList = [];
  form: FormGroup;
  recordId: number;
  action: string;
  bankShareReadOnly = true;
  vendorShareReadOnly = true;
  agentShareReadOnly = true;
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private alertService: AlertService,
    private fb: FormBuilder,
    private navigation: RouterServiceService,
    private securityService: SecurityService,
    private agentBanking: AgentBankingService,
    private storageService: StorageService,
  ) {

  }

  ngOnInit() {
    this.action = this.route.snapshot.params['id'];
    this.product = this.storageService.getSelectedProduct();
    this.populatePostingAmountItems();
    this.populatePostingAccountItems();
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
    }
    else if (this.action == "Edit") {
      this.isEdit = true;
      this.selectedData = this.storageService.getEntity();
      this.isAddMode = !this.selectedData;
    }
  }


  createForms() {
    this.form = this.fb.group({
      postingDetailId: [null],
      serviceId: this.product.serviceId,
      postingPriority: [null, Validators.required],
      sourceAccount: [null, Validators.required],
      destinationAccount: [null, Validators.required],
      amountType: [null, Validators.required],
      transCategory: [null, Validators.required],
      trustPostingCategory: [null],
      createdBy: [this.securityService.currentUser.userName],
      createDt: [this.securityService.currentUser.processDate],
      tranAmtVendorShare: [null],
      tranAmtBankShare: [null],
      tranAmtAgentShare: [null],
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


  findEntityData() {
    this.f.postingDetailId.setValue(this.selectedData.postingDetailId);
    this.f.serviceId.setValue(this.selectedData.serviceId);
    this.f.postingPriority.setValue(this.selectedData.postingPriority);
    this.f.sourceAccount.setValue(this.selectedData.sourceAccount.trim());
    this.f.destinationAccount.setValue(this.selectedData.destinationAccount.trim());
    this.f.amountType.setValue(this.selectedData.amountType.trim());
    this.onAmountTypeSelected();
    this.f.transCategory.setValue(this.selectedData.transCategory.trim());
    this.f.createdBy.setValue(this.selectedData.createdBy);
    this.f.trustPostingCategory.setValue(this.selectedData.trustPostingCategory);
    this.f.createDt.setValue(this.selectedData.createDt);
    this.f.tranAmtVendorShare.setValue(this.selectedData.tranAmtVendorShare);
    this.f.tranAmtBankShare.setValue(this.selectedData.tranAmtBankShare);
    this.f.tranAmtAgentShare.setValue(this.selectedData.tranAmtAgentShare);
    this.f.edit.setValue(true);
  }


  populatePostingAccountItems() {
    let request = new ServicePostingPolicy;
    request.serviceId = this.product.serviceId;
    this.loading[0] = true;
    this.agentBanking.findPostingPolicyAccountTypes(request)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          if (response.code !== "00") {
            return;
          }
          this.accountTypes = response.data;
        },
        error => {
          this.loading[0] = false;
          this.alertService.displayError(error);
        });
  }
  
  populatePostingAmountItems() {
    let request = new ServicePostingPolicy;
    request.serviceId = this.product.serviceId;
    this.loading[0] = true;
    this.agentBanking.findPostingPolicyAmountTypes(request)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          if (response.code !== "00") {
            return;
          }
          this.amountTypes = response.data;
        },
        error => {
          this.loading[0] = false;
          this.alertService.displayError(error);
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
    this.agentBanking.maintainServicePostingPolicy(this.prepareSubmitData())
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

  onAmountTypeSelected() {
    this.bankShareReadOnly = true;
    this.vendorShareReadOnly = true;
    this.agentShareReadOnly = true;
    this.f.tranAmtVendorShare.setValue(null);
    this.f.tranAmtBankShare.setValue(null);
    this.f.tranAmtAgentShare.setValue(null);
    if (this.f.amountType.value == "TRANS_AMOUNT_BANK_SHARE") {
      this.bankShareReadOnly = false;
      this.vendorShareReadOnly = true;
      //this.f.transAmtVendorShare.setValue(0);
    }

    else if (this.f.amountType.value == "TRANS_AMOUNT_VENDOR_SHARE") {
      this.vendorShareReadOnly = false;
      this.bankShareReadOnly = true;
      //this.f.transAmtBankShare.setValue(0);
    }

    else if (this.f.amountType.value == "TRANS_AMOUNT_AGENT_SHARE") {
      this.agentShareReadOnly = false;
    }
  }
  

  prepareSubmitData(): ServicePostingPolicy {
    let request = new ServicePostingPolicy;
    request.postingDetailId = this.f.postingDetailId.value;
    request.serviceId = this.f.serviceId.value;
    request.postingPriority = this.f.postingPriority.value;
    request.sourceAccount = this.f.sourceAccount.value;
    request.destinationAccount = this.f.destinationAccount.value;
    request.amountType = this.f.amountType.value;
    request.transCategory = this.f.transCategory.value;
    request.createdBy = this.f.createdBy.value;
    request.trustPostingCategory = this.f.trustPostingCategory.value;
    request.createDt = this.f.createDt.value;
    request.tranAmtVendorShare = this.f.tranAmtVendorShare.value;
    request.tranAmtBankShare = this.f.tranAmtBankShare.value;
    request.tranAmtAgentShare = this.f.tranAmtAgentShare.value;
    request.edit = this.isEdit;
    return request;
  }
  

  onClose() {
    this.navigation.goBack();
  }

}
