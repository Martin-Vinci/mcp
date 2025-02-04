import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Biller } from 'app/shared/models/biller';
import { BillerProduct } from 'app/shared/models/biller-products';
import { TransCode } from 'app/shared/models/trans-code';
import { AlertService } from 'app/shared/services/alert.service';
import { BillerService } from 'app/shared/services/biller.service';
import { RouterServiceService } from 'app/shared/services/router-service.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-biller-product-search',
  templateUrl: './biller-product-search.component.html',
  styleUrls: ['./biller-product-search.component.scss']
})
export class BillerProductSearchComponent implements OnInit {
  data: BillerProduct[];
  selectedData: BillerProduct;
  product: Biller;
  loading = [false, false, false, false]

  constructor(private router: Router,
    private storage: StorageService,
    private alertService: AlertService,
    public dialogService: DialogService, 
    public messageService: MessageService,
    private globalService: BillerService,
    private navigation: RouterServiceService
  ) {

  }

  
  ngOnInit() {
    this.product = this.storage.getSelectedProduct();
    this.onSearch();
  }


  onSearch() {
    let request = new BillerProduct;
    request.billerId = this.product.id;
    request.channelSource = "WEB";
    this.loading[0] = true;
    this.globalService.findBillerProducts(request)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          console.log(response);
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

  onEdit() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to modify');
      return;
    }
    this.storage.setEntity(this.selectedData);
    this.router.navigate(['biller-control/biller-product', 'Edit']);
  }

  onView() {  
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to view');
      return;
    }
    this.storage.setEntity(this.selectedData);
    this.router.navigate(['biller-control/biller-product', 'View']);
  }

  onAdd() {
    this.router.navigate(['biller-control/biller-product', 'Add']);
  }

  onClose() {
    this.navigation.goBack();
  }

}
