import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { LoanAccount } from 'app/shared/models/loan-account';
import { AlertService } from 'app/shared/services/alert.service';
import { LoanServiceService } from 'app/shared/services/loan-service.service';
import { DialogService, DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-modal-loan-account-search',
  templateUrl: './modal-loan-account-search.component.html',
  styleUrls: ['./modal-loan-account-search.component.scss']
})
export class ModalLoanAccountSearchComponent implements OnInit {
  customerType = [];
  data: LoanAccount[];
  selectedData: LoanAccount;
  loading = [false, false, false, false]
  form: FormGroup;
  constructor(private alertService: AlertService,
    private fb: FormBuilder,
    public dialogService: DialogService, 
    public ref: DynamicDialogRef, 
    public config: DynamicDialogConfig,
    private customerRefService: LoanServiceService
  ) {

  }

  ngOnInit() {
    this.createForms();  
    this.onSearch();  
  }

  createForms() {
    this.form = this.fb.group({
      id: [null],
      firstName: [null],
      phoneNo: [null],
      status: ["ACTIVE"],
      lastName: [null]
    });
  }

  onSearch() {
    this.loading[0] = true;
    this.customerRefService.findLoanAccounts(this.form.value)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.data = response.data;
        },
        error => {
          this.loading[0] = false;
          this.alertService.displayError(error);
        });
  }


  cancel() {
    this.ref.close(false);
  }

  submit() {
    this.ref.close(this.selectedData);
  }

}
