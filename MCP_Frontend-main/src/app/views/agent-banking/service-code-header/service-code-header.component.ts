import { Component, OnInit } from "@angular/core";
import { TransCode } from "app/shared/models/trans-code";
import { StorageService } from "app/shared/services/storage.service";

@Component({
  selector: 'app-service-code-header',
  templateUrl: './service-code-header.component.html',
  styleUrls: ['./service-code-header.component.scss']
})
export class ServiceCodeHeaderComponent implements OnInit {
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
