import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { TransCode } from 'app/shared/models/trans-code';
import { TransactionRef, TransData } from 'app/shared/models/transaction-ref';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { StorageService } from 'app/shared/services/storage.service';
import { TransactionService } from 'app/shared/services/transaction.service';
import { MessageService } from 'primeng/api';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';
import { TransactionsDetailsComponent } from '../transactions-details/transactions-details.component';
import * as FileSaver from 'file-saver'
import { CenteTrustTrans } from 'app/shared/models/cente-trust-trans';
import { CenteTrustSummary } from 'app/shared/models/cente-trust-summary';
import { RouterServiceService } from 'app/shared/services/router-service.service';


@Component({
  selector: 'app-cente-trust-transaction-listing',
  templateUrl: './cente-trust-transaction-listing.component.html',
  styleUrls: ['./cente-trust-transaction-listing.component.scss']
})
export class CenteTrustTransactionListingComponent implements OnInit {

  customerType = [];
  data: CenteTrustTrans[];
  transCodes: TransCode[];
  trustSummary: CenteTrustSummary;
  selectedData: CenteTrustTrans;

  loading = [false, false, false, false]
  ref: DynamicDialogRef;
  form: FormGroup;
  constructor(private router: Router,
    private storage: StorageService,
    private fb: FormBuilder,
    private alertService: AlertService,
    private navigation: RouterServiceService,
    public dialogService: DialogService,
    public messageService: MessageService,
    private storageService: StorageService,
    private transactionService: TransactionService,
    private agentBankingService: AgentBankingService,
  ) {

  }

  ngOnInit() {
    this.trustSummary = this.storageService.getEntity();
    this.createForms();
    this.findServices();
    this.onSearch();
  }

  getDate(): string {
    var today = new Date();
    var dd = String(today.getDate()).padStart(2, '0');
    var mm = String(today.getMonth() + 1).padStart(2, '0'); //January is 0!
    var yyyy = today.getFullYear();
    return yyyy + '-' + mm + '-' + dd;
  }


  createForms() {
    this.form = this.fb.group({
      serviceCode: [null],
      id: [this.trustSummary.batchId]
    });
  }

  dynamicSort(property) {
    var sortOrder = 1;
    if(property[0] === "-") {
        sortOrder = -1;
        property = property.substr(1);
    }
    return function (a,b) {
        /* next line works with strings and numbers, 
         * and you may want to customize it to your needs
         */
        var result = (a[property] < b[property]) ? -1 : (a[property] > b[property]) ? 1 : 0;
        return result * sortOrder;
    }
}


  findServices() {
    let request = new TransCode;
    this.loading[0] = true;
    this.agentBankingService.findServices(request)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          if (response.code !== "00") {
            return;
          }
          this.transCodes = response.data;

          this.transCodes.sort(this.dynamicSort("description"))
        },
        error => {
          this.loading[0] = false;
          this.alertService.displayError(error);
        });
  }


  onSearch() {
    this.loading[0] = true;
    this.data = [];
    this.transactionService.findTransCenteTrustDetails(this.form.value)
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

  onClose() {
    this.navigation.goBack();
  }


  onEdit() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to modify');
      return;
    }
    this.storage.setEntity(this.selectedData);
    this.router.navigate(['system-admin/user', 'Edit']);
  }


  exportExcel() {
    import("xlsx").then(xlsx => {
      const worksheet = xlsx.utils.json_to_sheet(this.data);
      const workbook = { Sheets: { data: worksheet }, SheetNames: ["data"] };
      const excelBuffer: any = xlsx.write(workbook, {
        bookType: "xlsx",
        type: "array"
      });
      this.saveAsExcelFile(excelBuffer, "Transaction-List");
    });
  }

  saveAsExcelFile(buffer: any, fileName: string): void {
    let EXCEL_TYPE = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8';
    let EXCEL_EXTENSION = '.xlsx';
    const data: Blob = new Blob([buffer], {
      type: EXCEL_TYPE
    });
    FileSaver.saveAs(data, fileName + '_export_' + new Date().getTime() + EXCEL_EXTENSION);
  }


}
