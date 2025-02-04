import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { LoanAccount } from 'app/shared/models/loan-account';
import { LoanSchedule } from 'app/shared/models/loan-account-schedule';
import { AlertService } from 'app/shared/services/alert.service';
import { LoanServiceService } from 'app/shared/services/loan-service.service';
import { DialogService, DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';


@Component({
  selector: 'app-modal-loan-schedule',
  templateUrl: './modal-loan-schedule.component.html',
  styleUrls: ['./modal-loan-schedule.component.scss']
})
export class ModalLoanScheduleComponent implements OnInit {
  data: LoanSchedule[];
  selectedData: LoanSchedule;
  filter: LoanAccount;
  totalPrincipal;
  loading = [false, false, false, false]
  constructor(private alertService: AlertService,
    private fb: FormBuilder,
    public dialogService: DialogService, 
    public ref: DynamicDialogRef, 
    public config: DynamicDialogConfig,
    private customerRefService: LoanServiceService
  ) {

  }

  ngOnInit() {
    this.filter = this.config.data;
   this.onSearch();
   this.getTotalPrincipal();
  }


  onSearch() {
    this.loading[0] = true;
    let request = new LoanSchedule();
    request.loanId = this.filter.loanId;
    this.customerRefService.findLoanSchedule(request)
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


  getTotalPrincipal() {
  //   let total = 0;
  //   for (let item of Object.keys(this.data)) {
  //     var eventItem = this.data[item];
  //     total += eventItem.principalAmount;
  // }
  //   this.totalPrincipal = total;
}

calculateThisYearTotal() {
    let total = 0;
    for(let sale of this.data) {
        total += sale.principalAmount;
    }

    this.totalPrincipal = total;
}


  cancel() {
    this.ref.close(false);
  }

  submit() {
    this.ref.close(this.selectedData);
  }


  exportExcel() {
    import("xlsx").then(xlsx => {
      const worksheet = xlsx.utils.json_to_sheet(this.data);
      const workbook = { Sheets: { data: worksheet }, SheetNames: ["data"] };
      const excelBuffer: any = xlsx.write(workbook, {
        bookType: "xlsx",
        type: "array"
      });
      this.saveAsExcelFile(excelBuffer, this.filter.loanNumber + "-loan-schedules");
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
