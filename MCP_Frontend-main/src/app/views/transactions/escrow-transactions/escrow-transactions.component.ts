import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Charge } from 'app/shared/models/charge';
import { EscrowData } from 'app/shared/models/escrow-data';
import { TransactionDetails } from 'app/shared/models/transaction-details';
import { TransactionRef, TransData } from 'app/shared/models/transaction-ref';
import { UserData } from 'app/shared/models/user-data';
import { AgentbakingService } from 'app/shared/services/agentbaking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { StorageService } from 'app/shared/services/storage.service';
import { SystemAdminService } from 'app/shared/services/system-admin.service';
import { TransactionService } from 'app/shared/services/transaction.service';
import { MessageService } from 'primeng/api';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';
import { TransactionsDetailsComponent } from '../transactions-details/transactions-details.component';

@Component({
  selector: 'app-escrow-transactions',
  templateUrl: './escrow-transactions.component.html',
  styleUrls: ['./escrow-transactions.component.scss']
})
export class EscrowTransactionsComponent implements OnInit {

  customerType = [];
  data: EscrowData[];
  selectedData: EscrowData;

  loading = [false, false, false, false]
  ref: DynamicDialogRef;
  constructor(private router: Router,
    private storage: StorageService,
    private alertService: AlertService,
    public dialogService: DialogService,
    public messageService: MessageService,
    private transactionService: TransactionService,
  ) {

  }

  ngOnInit() {
    this.onSearch();
  }

  onSearch() {
    let request = new EscrowData;
    this.loading[0] = true;
    this.transactionService.findEscrowTransactions(request)
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

  onApprove() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a transaction to approve');
      return;
    }
    this.loading[0] = true;
    this.selectedData.action = "APPROVED";
    this.transactionService.approveEscrowTransaction(this.selectedData)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.alertService.displaySuccess("Transaction has been approved");
          this.onSearch();
        },
        error => {
          this.loading[0] = false;
          this.alertService.displayError(error);
        });
  }
  onDecline() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a transaction to decline');
      return;
    }
    this.loading[0] = true;
    this.selectedData.action = "DECLINED";
    this.transactionService.approveEscrowTransaction(this.selectedData)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.alertService.displaySuccess("Transaction has been declined");
          this.onSearch();
        },
        error => {
          this.loading[0] = false;
          this.alertService.displayError(error);
        });
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
    import("file-saver").then(FileSaver => {
      let EXCEL_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8";
      let EXCEL_EXTENSION = ".xlsx";
      const data: Blob = new Blob([buffer], {
        type: EXCEL_TYPE
      });
      FileSaver.saveAs(
        data,
        fileName + "_export_" + new Date().getTime() + EXCEL_EXTENSION
      );
    });
  }


}
