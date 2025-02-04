import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { EfrisInvoice } from 'app/shared/models/efris-invoice';
import { AlertService } from 'app/shared/services/alert.service';
import { EfrisService } from 'app/shared/services/efris.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';
import { saveAs } from 'file-saver';

@Component({
  selector: 'app-supplier-payment-history',
  templateUrl: './supplier-payment-history.component.html',
  styleUrls: ['./supplier-payment-history.component.scss']
})
export class SupplierPaymentHistoryComponent implements OnInit {
  data: EfrisInvoice[];
  selectedData: EfrisInvoice;
  loading = [false, false, false, false]
  form: FormGroup;
  constructor(private router: Router,
    private storage: StorageService,
    private alertService: AlertService,
    private fb: FormBuilder,
    public dialogService: DialogService,
    public messageService: MessageService,
    private efrisService: EfrisService,
  ) {

  }

  ngOnInit() {
    this.createForms();
    sessionStorage.removeItem('GoodsDetails');
    this.onSearch();
  }


  createForms() {
    this.form = this.fb.group({
      phoneNumber: [null],
      customerName: [null],
    });
  }

  get f() {
    return this.form.controls;
  }


  onSearch() {
    this.loading[0] = true;
    this.efrisService.findAllInvoices(this.form.value)
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

  downloadFile(): void {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select an invoice to download');
      return;
    }
    this.efrisService
      .invoiceDownLoad(this.selectedData.invoiceNo.trim())
      .subscribe(blob => saveAs(blob, this.selectedData.invoiceNo + ".pdf"));
  }


  onView() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to view');
      return;
    }
    this.storage.setSelectedProduct(this.selectedData);
    this.router.navigate(['transaction-control/supplier-payment', 'View']);
  }

  onAdd() {
    this.router.navigate(['transaction-control/supplier-payment', 'Add']);
  }

}
