import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LoanAccount } from 'app/shared/models/loan-account';

import { CreditAppl } from 'app/shared/models/loan-application';
import { LoanRepayment } from 'app/shared/models/loan-repayment';
import { AlertService } from 'app/shared/services/alert.service';
import { LoanServiceService } from 'app/shared/services/loan-service.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-loan-repayment',
  templateUrl: './loan-repayment.component.html',
  styleUrls: ['./loan-repayment.component.scss']
})
export class LoanRepaymentComponent implements OnInit {
  customerType = [];
  data: LoanRepayment[];
  selectedData: LoanRepayment;
  loading = [false, false, false, false]

  constructor(private router: Router,
    private storageService: StorageService,
    private alertService: AlertService,
    public dialogService: DialogService, 
    public messageService: MessageService,
    private globalService: LoanServiceService,
  ) {

  }

  ngOnInit() {
    this.onSearch();
  }

  onSearch() {
    let request = new LoanRepayment;
    this.loading[0] = true;
    this.globalService.findTransHistory(request)
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


  public onAdd() {
    this.router.navigate(['webcash/loan-repayment', 'Add']);
  }

  exportExcel() {
    import("xlsx").then(xlsx => {
      const worksheet = xlsx.utils.json_to_sheet(this.data);
      const workbook = { Sheets: { data: worksheet }, SheetNames: ["data"] };
      const excelBuffer: any = xlsx.write(workbook, {
        bookType: "xlsx",
        type: "array"
      });
      this.saveAsExcelFile(excelBuffer, "Credit Repayment History");
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

  public onEdit() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('No record has been selected');
      return;
    }
    this.storageService.setEntity(this.selectedData);
    this.router.navigate(['webcash/loan-repayment', 'Edit']);
  }

}
