import { DatePipe } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { FormGroup, FormBuilder, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { DatePipeService } from "app/shared/helpers/date-pipe.service";
import { Customer } from "app/shared/models/customer";
import { LoanAccount } from "app/shared/models/loan-account";
import { LoanRepayment } from "app/shared/models/loan-repayment";
import { AlertService } from "app/shared/services/alert.service";
import { LoanServiceService } from "app/shared/services/loan-service.service";
import { SecurityService } from "app/shared/services/security.service";
import { StorageService } from "app/shared/services/storage.service";
import { MenuItem } from "primeng/api";
import { DialogService, DynamicDialogRef } from "primeng/dynamicdialog";
import { first } from "rxjs/operators";
import { ModalLoanAccountSearchComponent } from "../modal-loan-account-search/modal-loan-account-search.component";


@Component({
  selector: 'app-loan-repayment-details',
  templateUrl: './loan-repayment-details.component.html',
  styleUrls: ['./loan-repayment-details.component.scss']
})
export class LoanRepaymentDetailsComponent implements OnInit {
  loading = false;
  submitted = false;
  customerName = false;
  selectedData = new LoanRepayment;
  customer: LoanAccount;
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
    private router: Router,
    private alertService: AlertService,
    private fb: FormBuilder,
    private datepipe: DatePipe,
    private customerAPI: LoanServiceService,
    private storageService: StorageService,
    private securityService: SecurityService,
    public dialogService: DialogService,
    private date: DatePipeService
  ) {

  }

  ngOnInit() {
    this.createForm();
    this.onGetCustNo();
  }

  ngOnDestroy() {
    this.storageService.closeEntity();
  }

  onGetCustNo() {
    this.ref = this.dialogService.open(ModalLoanAccountSearchComponent, {
      header: 'Get Loan Accounts',
      width: '40%',
    });
    this.ref.onClose.subscribe((selection: LoanAccount) => {
      if (selection) {
        this.customer = selection;
        this.customerName = true;
        this.f.acctNo.setValue(selection.loanNumber);
      } else {
        this.onClose();
      }
    });
  }

  assignSelectedCustomerId(request: Customer) {
    this.f.custId.setValue(request.id);
  }

  createForm() {
    this.form = this.fb.group({
      repaymentId: null,
      acctNo: null,
      referenceNo: this.datepipe.transform(new Date(),'yyyyMMddHHmmss'),
      amount: [null, Validators.required],
      transDate: null,
      channelCode: null,
    });
  }

  get f() {
    return this.form.controls;
  }


  determineMaturityDate() {
    let contractDate = this.f.startDate.value;
    let period = this.f.repayPeriod.value;
    let term: number = this.f.repayTerm.value;
    if (contractDate == null || period == null || term == null) {
      return;
    }
    this.f.endDate.setValue(this.date.getNextPeriodDate(contractDate, period, term));
  }

 
  validation_messages = {
    amount: [{ type: 'required', message: 'Credit amount is required' }],
    startDate: [{ type: 'required', message: 'Start date is required' }],
    repayTerm: [{ type: 'required', message: 'Term is required' }],
    repayPeriod: [{ type: 'required', message: 'Frequency is required' }],
    purpose: [{ type: 'required', message: 'Purpose is required' }],
    status: [{ type: 'required', message: 'Status is required' }],
  };


  postLoanRepayment() {
    this.submitted = true;
    if (this.form.invalid) {
      this.alertService.displayInfo("Please fill all the fields");
      this.loading = false;
      return;
    }
    this.loading = true;
    let request = this.prepareSubmitData();
    this.customerAPI.postLoanRepayment(request)
      .pipe(first())
      .subscribe(
        response => {
          this.loading = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.alertService.displaySuccess("Repayment posted successfully. Please check the schedules");
          this.submitted = false;
          this.onClose();
        },
        error => {
          this.loading = false;
          this.submitted = false;
          this.alertService.displayError(error);
        });
  }


  prepareSubmitData(): LoanRepayment {
    let request = new LoanRepayment;
    request.acctNo = this.f.acctNo.value;
    request.transAmount = this.f.amount.value;
    request.chargeAmount = 0;
    request.channelCode = "WEBCASH-PORTAL";
    request.postedBy = this.securityService.currentUser.userName;
    //request.initiatorPhoneNo = this.f.acctNo.value;
    request.transDescr = "Loan Repayment [" + this.f.acctNo.value + " ]";
    request.externalTransRef = this.f.acctNo.value;
    return request;
  }

  onClose() {
    this.router.navigate(['webcash/loan-repayment-search']);
  }

}
