import { DatePipe } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { FormGroup, FormBuilder, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { DatePipeService } from "app/shared/helpers/date-pipe.service";
import { Customer } from "app/shared/models/customer";
import { CreditAppl } from "app/shared/models/loan-application";
import { AlertService } from "app/shared/services/alert.service";
import { LoanServiceService } from "app/shared/services/loan-service.service";
import { NavigationService } from "app/shared/services/navigation.service";
import { RouterServiceService } from "app/shared/services/router-service.service";
import { SecurityService } from "app/shared/services/security.service";
import { StorageService } from "app/shared/services/storage.service";
import { parse } from "date-fns";
import { MenuItem } from "primeng/api";
import { DialogService, DynamicDialogRef } from "primeng/dynamicdialog";
import { first } from "rxjs/operators";
import { ModalCustomerSearchComponent } from "../modal-customer-search/modal-customer-search.component";


@Component({
  selector: 'app-credit-applic-maintain',
  templateUrl: './credit-applic-maintain.component.html',
  styleUrls: ['./credit-applic-maintain.component.scss']
})
export class CreditApplicMaintainComponent implements OnInit {
  loading = [false, false, false, false, false];
  submitted = false;
  customerName = false;
  selectedData = new CreditAppl;
  customer: Customer;
  isEdit = false;
  isView = false;
  isAddMode = false;
  enableApprove = false;
  form: FormGroup;
  recordId: number;
  action: string;
  items: MenuItem[];
  statusPickList = [];
  categories = [];
  ref: DynamicDialogRef;
  constructor(
    private route: ActivatedRoute,
    private alertService: AlertService,
    private fb: FormBuilder,
    private navigation: RouterServiceService,
    private securityService: SecurityService,
    private customerAPI: LoanServiceService,
    private storageService: StorageService,
    public dialogService: DialogService,
    private date: DatePipeService
  ) {

  }

  ngOnInit() {
    this.action = this.route.snapshot.params['id'];
    this.prepareFormBasedOnAction();
    this.createForm();
    if (this.isAddMode == false) {
      this.findEntityData();
    }
  }

  ngOnDestroy() {
    this.storageService.closeEntity();
  }

  prepareFormBasedOnAction() {
    if (this.action == "Add") {
      this.isAddMode = true;
      this.onGetCustNo();
      return;
    }

    if (this.action == "View") {
      this.isView = true;
      this.selectedData = this.storageService.getEntity();
      this.isAddMode = !this.selectedData;
    }
    if (this.action == "Edit") {
      this.isEdit = true;
      this.selectedData = this.storageService.getEntity();
      this.isAddMode = !this.selectedData;
      if (this.selectedData.status.toUpperCase() == "PENDING")
        this.enableApprove = true;
      return;
    }
  }


  onGetCustNo() {
    this.ref = this.dialogService.open(ModalCustomerSearchComponent, {
      header: 'Search Customer',
      width: '50%',
    });
    this.ref.onClose.subscribe((selection: Customer) => {
      if (selection) {
        this.customer = selection;
        this.customerName = true;
        this.f.custId.setValue(selection.id);
      }
    });
  }

  assignSelectedCustomerId(request: Customer) {
    this.f.custId.setValue(request.id);
  }

  createForm() {
    this.form = this.fb.group({
      id: null,
      creditType: ["MOBILE_LOAN", Validators.required],
      startDate: [null, Validators.required],
      endDate: null,
      repayTerm: [null, Validators.required],
      repayPeriod: [null, Validators.required],
      nextPmtAmt: null,
      status: "PENDING",
      custId: null,
      loanPurpose: null,
      createdBy: this.securityService.currentUser.userName,
      modifiedBy: null,
      modifiedDate: null,
      rowVersion: 0,
      applAmt: [null, Validators.required],
    });
  }

  get f() {
    return this.form.controls;
  }


  determineMaturityDate() {
    console.log("========================= " + this.f.startDate.value);
    let contractDate = this.date.isoDateString(this.f.startDate.value);
    let period = this.f.repayPeriod.value;
    let term: number = this.f.repayTerm.value;
    if (contractDate == null || period == null || term == null) {
      return;
    }
    this.f.endDate.setValue(this.date.getNextPeriodDate(parse(contractDate.toString()), period, term));
  }


  validation_messages = {
    applAmt: [{ type: 'required', message: 'Credit amount is required' }],
    startDate: [{ type: 'required', message: 'Start date is required' }],
    endDate: [{ type: 'required', message: 'End date is required' }],
    repayTerm: [{ type: 'required', message: 'Term is required' }],
    repayPeriod: [{ type: 'required', message: 'Frequency is required' }],
    purpose: [{ type: 'required', message: 'Purpose is required' }],
    status: [{ type: 'required', message: 'Status is required' }],
  };


  findEntityData() {
    console.log(this.selectedData);
    this.f.id.setValue(this.selectedData.id);
    this.f.creditType.setValue(this.selectedData.creditType);
    this.f.startDate.setValue(this.selectedData.startDate);
    this.f.endDate.setValue(this.selectedData.endDate);
    this.f.repayTerm.setValue(this.selectedData.repayTerm);
    this.f.repayPeriod.setValue(this.selectedData.repayPeriod);
    this.f.nextPmtAmt.setValue(this.selectedData.nextPmtAmt);
    this.f.status.setValue(this.selectedData.status.toUpperCase());
    this.f.custId.setValue(this.selectedData.custId);
    this.f.createdBy.setValue(this.selectedData.createdBy);
    //this.f.purpose.setValue(this.selectedData.purpose);
    this.f.applAmt.setValue(this.selectedData.applAmt);
    this.f.loanPurpose.setValue(this.selectedData.loanPurpose);
  }

  onSubmit() {
    this.submitted = true;
    if (this.form.invalid) {
      this.alertService.displayInfo("Please fill all the fields");
      this.loading[0] = false;
      return;
    }
    this.loading[0] = true;
    this.customerAPI.creditApplication(this.prepareSubmitData())
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


  onReview(status: string) {
    this.submitted = true;
    if (this.form.invalid) {
      this.alertService.displayInfo("Please fill all the fields");
      this.loading[1] = false;
      return;
    }
    this.loading[1] = true;
    let request = this.prepareSubmitData();
    request.status = status;
    this.customerAPI.approveLoan(request)
      .pipe(first())
      .subscribe(
        response => {
          console.log(response);
          this.loading[1] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.alertService.displaySuccess("Record Saved Successfully");
          this.submitted = false;
          this.onClose();
        },
        error => {
          this.loading[1] = false;
          this.submitted = false;
          this.alertService.displayError(error);
        });
  }


  prepareSubmitData(): CreditAppl {
    let request = new CreditAppl;
    request.id = this.f.id.value;
    request.creditType = this.f.creditType.value;
    request.startDate = this.date.isoDateString(this.f.startDate.value);
    request.endDate = this.date.isoDateString(this.f.endDate.value);
    request.repayTerm = this.f.repayTerm.value;
    request.repayPeriod = this.f.repayPeriod.value;
    request.status = this.f.status.value;
    request.custId = this.f.custId.value;
    request.createdBy = this.f.createdBy.value;
    request.loanPurpose = this.f.loanPurpose.value;
    request.applAmt = this.f.applAmt.value;
    return request;
  }

  onClose() {
    this.navigation.goBack();
  }

}
