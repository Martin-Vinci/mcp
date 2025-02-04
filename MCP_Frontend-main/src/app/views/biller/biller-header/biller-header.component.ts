import { Component, OnInit } from "@angular/core";
import { TransCode } from "app/shared/models/trans-code";
import { StorageService } from "app/shared/services/storage.service";

@Component({
  selector: 'app-biller-header',
  templateUrl: './biller-header.component.html',
  styleUrls: ['./biller-header.component.scss']
})
export class BillerHeaderComponent implements OnInit {
  selectedData = new TransCode;
  constructor(
    private storageService: StorageService,

  ) {

  }

  ngOnInit() {
    this.selectedData = this.storageService.getSelectedProduct();
  }

  ngOnDestroy() {
    //this.storageService.closeEntity();
  }
}
