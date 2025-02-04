import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { GoodsData } from 'app/shared/models/goods-data';
import { AlertService } from 'app/shared/services/alert.service';
import { EfrisService } from 'app/shared/services/efris.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-goods-and-services-listing',
  templateUrl: './goods-and-services-listing.component.html',
  styleUrls: ['./goods-and-services-listing.component.scss']
})
export class GoodsAndServicesListingComponent implements OnInit {
  data: GoodsData[]; 
  selectedData: GoodsData;
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
    this.onSearch();
  }

  createForms() {
    this.form = this.fb.group({
      pageNo: [null],
      customerName: [null],
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

  onView() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to view');
      return;
    }
    this.storage.setSelectedProduct(this.selectedData);
    this.router.navigate(['transaction-control/efris-services', 'View']);
  }

  onAdd() {
    this.router.navigate(['transaction-control/efris-services', 'Add']);
  }

}
