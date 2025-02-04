import { Component, OnInit } from "@angular/core";
import { FormGroup, FormBuilder, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { DatePipeService } from "app/shared/helpers/date-pipe.service";
import { Customer } from "app/shared/models/customer";
import { LoanAccount } from "app/shared/models/loan-account";
import { CreditAppl } from "app/shared/models/loan-application";
import { AlertService } from "app/shared/services/alert.service";
import { LoanServiceService } from "app/shared/services/loan-service.service";
import { StorageService } from "app/shared/services/storage.service";
import { MenuItem } from "primeng/api";
import { DialogService, DynamicDialogRef } from "primeng/dynamicdialog";
import { first } from "rxjs/operators";
import { ModalLoanScheduleComponent } from "../modal-loan-schedule/modal-loan-schedule.component";


@Component({
  selector: 'app-loan-account-maintain',
  templateUrl: './loan-account-maintain.component.html',
  styleUrls: ['./loan-account-maintain.component.scss']
})
export class LoanAccountMaintainComponent implements OnInit {
  loading = false;
  submitted = false;
  customerName = false;
  selectedData = new LoanAccount;
  customer: Customer;
  isEdit = false;
  isView = false;
  isAddMode = false;
  enableDisburse = false;
  form: FormGroup;
  recordId: number;
  action: string;
  items: MenuItem[];
  statusPickList = [];
  categories = [];
  ref: DynamicDialogRef;
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private alertService: AlertService,
    private fb: FormBuilder,
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
        this.enableDisburse = true;
      return;
    }
  }


  onGetSchedules() {
    this.ref = this.dialogService.open(ModalLoanScheduleComponent, {
      header: 'Loan Schedules',
      width: '60%',
      data: this.selectedData,
    });
    this.ref.onClose.subscribe((selection: Customer) => {
      if (selection) {
        this.customer = selection;
        this.customerName = true;
        this.f.custId.setValue(selection.id);
      }
    });
  }


  createForm() {
    this.form = this.fb.group({
      loanId: null,
      loanNumber: null,
      creditApplId: null,
      approvedAmount: null,
      startDate: null,
      endDate: null,
      repayInterest: null,
      repayPrincipal: null,
      term: null,
      repayPeriod: null,
      status: null,
      createdBy: null,
      createDate: null,
      creditNumber: null,
      custId: null,
      custName: null
    });
  }

  get f() {
    return this.form.controls;
  }


  findEntityData() {
    this.f.loanId.setValue(this.selectedData.loanId);
    this.f.approvedAmount.setValue(this.selectedData.approvedAmount);
    this.f.loanNumber.setValue(this.selectedData.loanNumber);
    this.f.startDate.setValue(this.selectedData.startDate);
    this.f.endDate.setValue(this.selectedData.endDate);
    this.f.term.setValue(this.selectedData.term);
    this.f.repayPeriod.setValue(this.selectedData.repayPeriod);
    //this.f.nextPmtAmt.setValue(this.selectedData.nextPmtAmt);
    this.f.status.setValue(this.selectedData.status.toUpperCase());
    this.f.custId.setValue(this.selectedData.custId);
    this.f.createdBy.setValue(this.selectedData.createdBy);
    this.f.createDate.setValue(this.selectedData.createDate);
  }


  disburseLoan() {
    this.submitted = true;
    if (this.form.invalid) {
      this.alertService.displayInfo("Please fill all the fields");
      this.loading = false;
      return;
    }
    this.loading = true;
    this.customerAPI.disburseLoan(this.selectedData)
      .pipe(first())
      .subscribe(
        response => {
          this.loading = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.alertService.displaySuccess("Record Saved Successfully");
          this.submitted = false;
          this.onClose();
        },
        error => {
          this.loading = false;
          this.submitted = false;
          this.alertService.displayError(error);
        });
  }

  prepareSubmitData(): CreditAppl {
    let request = new CreditAppl;
    request.id = this.f.id.value;
    request.creditType = this.f.creditType.value;
    request.startDate = this.f.startDate.value;
    request.endDate = this.f.endDate.value;
    request.repayTerm = this.f.repayTerm.value;
    request.repayPeriod = this.f.repayPeriod.value;
    request.nextPmtAmt = this.f.nextPmtAmt.value;
    request.status = this.f.status.value;
    request.custId = this.f.custId.value;
    request.createdBy = this.f.createdBy.value;
    request.createDate = this.f.createDate.value;
    request.rowVersion = this.f.rowVersion.value;
    request.applAmt = this.f.applAmt.value;
    return request;
  }

  onClose() {
    this.router.navigate(['webcash/loan-account-search']);
  }

}
