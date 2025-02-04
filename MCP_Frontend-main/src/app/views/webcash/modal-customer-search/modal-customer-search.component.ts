import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Customer } from 'app/shared/models/customer';
import { AlertService } from 'app/shared/services/alert.service';
import { CustomerService } from 'app/shared/services/customer-service.service';
import { DialogService, DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';


@Component({
  selector: 'app-modal-customer-search',
  templateUrl: './modal-customer-search.component.html',
  styleUrls: ['./modal-customer-search.component.scss']
})
export class ModalCustomerSearchComponent implements OnInit {
  customerType = [];
  data: Customer[];
  selectedData: Customer;
  loading = [false, false, false, false]
  form: FormGroup;
  constructor(private alertService: AlertService,
    private fb: FormBuilder,
    public dialogService: DialogService, 
    public ref: DynamicDialogRef, 
    public config: DynamicDialogConfig,
    private customerRefService: CustomerService
  ) {

  }

  ngOnInit() {
    this.createForms();  
   
  }

  createForms() {
    this.form = this.fb.group({
      id: [null],
      firstName: [null],
      phoneNo: [null],
      lastName: [null]
    });
  }

  onSearch() {
    this.loading[0] = true;
    this.customerRefService.findAllCustomers(this.form.value)
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
