import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { GoodsData } from 'app/shared/models/goods-data';
import { LoanAccount } from 'app/shared/models/loan-account';
import { AlertService } from 'app/shared/services/alert.service';
import { EfrisService } from 'app/shared/services/efris.service';
import { LoanServiceService } from 'app/shared/services/loan-service.service';
import { DialogService, DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-modal-efris-goods-and-service',
  templateUrl: './modal-efris-goods-and-service.component.html',
  styleUrls: ['./modal-efris-goods-and-service.component.scss']
})
export class ModalEfrisGoodsAndServiceComponent implements OnInit {
  customerType = [];
  data: GoodsData[]; 
  selectedData: GoodsData;
  loading = [false, false, false, false]
  form: FormGroup;
  constructor(private alertService: AlertService,
    private fb: FormBuilder,
    public dialogService: DialogService, 
    public ref: DynamicDialogRef, 
    public config: DynamicDialogConfig,
    private efrisService: EfrisService,
  ) {

  }

  ngOnInit() {
    this.createForms();  
    this.onSearch();  
  }

  createForms() {
    this.form = this.fb.group({
      pageNo: [null]
    });
  }

  get f() {
    return this.form.controls;
  }

  onSearch() {
    this.loading[0] = true;
    this.f.pageNo.setValue(this.f.pageNo.value == "" ? null : this.f.pageNo.value);
    this.efrisService.goodsAndServiceInquiry(this.form.value)
      .pipe(first())
      .subscribe(
        response => {
          console.log(response);
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
