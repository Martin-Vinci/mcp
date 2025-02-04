import { SelectionModel } from '@angular/cdk/collections';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatPaginator } from '@angular/material/paginator';
import { Router } from '@angular/router';
import { first } from 'rxjs/operators';
import * as FileSaver from 'file-saver';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable'
import { AlertService } from 'app/shared/services/alert.service';
import { ReportService } from 'app/shared/services/reports.service';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { TransCode } from 'app/shared/models/trans-code';
import { RPTTransaction, TransactionBand } from 'app/shared/models/transaction-ref';
import { MobileUser } from 'app/shared/models/mobile-user';



const head = [['First Name', 'Last Name', 'Phone Number', 'Email Address', 'Plot Number',
  'House Number', 'Membership Status', 'Status']]

  @Component({
    selector: 'app-transaction-band-report',
    templateUrl: './transaction-band-report.component.html',
    styleUrls: ['./transaction-band-report.component.scss']
  })
  export class TransactionBandReportComponent implements OnInit {
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  shiftScheduleSelection = new SelectionModel<RPTTransaction>(false, []);
  selectedScheduleDetail = new RPTTransaction;
  scheduleId: number;
  departmentId: number;
  loading = false;
  serviceList: TransCode[];
  isTableLoading = false;
  agentsLoading = false;
  form: FormGroup;
  cols: any[];
  customers: TransactionBand[] = [];
  constructor(private alertService: AlertService,
    private router: Router,
    private agentService: AgentBankingService,
    private reportService: ReportService,
    private fb: FormBuilder) {
  }

  ngOnInit(): void {
    this.createForm();
    this.findReportData();
    this.findServiceCodes();
    this.cols = [
      { field: "dateOccured", header: "Date" },
      { field: "referenceNo", header: "Reference" },
      { field: "incidentSummary", header: "Incident Summary" },
      { field: "incidentImpact", header: "Impact" },
      { field: "location", header: "Location" },
      { field: "status", header: "Status" },
      { field: "reporterName", header: "Reported By" }
    ];
  }

  getDate(): string {
    var today = new Date();
    var dd = String(today.getDate()).padStart(2, '0');
    var mm = String(today.getMonth() + 1).padStart(2, '0'); //January is 0!
    var yyyy = today.getFullYear();
    return yyyy + '-' + mm + '-' + dd;
  }

  
  findServiceCodes() {
    let expenseSearch = new TransCode();
    this.serviceList = [];
    this.agentService.findServices(expenseSearch)
      .pipe(first())
      .subscribe(
        response => {
          if (response.code !== "00")
            return;
          let arrayItems = [];
          for (const element of response.data) {
            if (element.status == 'Active')
              arrayItems.push(element);
          }
          this.serviceList = arrayItems;
        });
  }

  createForm() {
    this.form = this.fb.group({
      id: null,
      crAcctNo: null,
      drAcctNo: null,
      amount: null,
      postedBy: null,
      serviceCode: null,
      successFlag: null,
      startDate: [this.getDate()],
      endDate: [this.getDate()],
    });
  }

  get f() {
    return this.form.controls;
  }


 
  findReportData() {
    this.isTableLoading = true;
    this.customers = [];
    this.reportService.findTransactionBandReport(this.form.value)
      .pipe(first())
      .subscribe(
        response => {
          this.isTableLoading = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.customers = response.data;
        },
        error => {
          this.isTableLoading = false;
          this.alertService.displayError(error);
        }
      );
  }

  onClose() {
    this.router.navigate(['accounts/schedules-list']);
  }

  public exportPdf() {
    const doc = new jsPDF('l', 'mm', 'a4');
    let data = [];
    let item: any;
    for (const element of this.customers) {
      item = [element.description, element.transCount, element.transAmount];
      data.push(item);
    }
    autoTable(doc, {
      head: head,
      body: data,
      didDrawCell: () => { },
    });

    doc.save('data.pdf');
  }


  exportExcel() {
    import("xlsx").then(xlsx => {
      const worksheet = xlsx.utils.json_to_sheet(this.customers);
      const workbook = { Sheets: { data: worksheet }, SheetNames: ["data"] };
      const excelBuffer: any = xlsx.write(workbook, {
        bookType: "xlsx",
        type: "array"
      });
      this.saveAsExcelFile(excelBuffer, "data");
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

  getExportTable(data: RPTTransaction[]): RPTTransaction[] {
    let exportTable: RPTTransaction[] = [];
    let item = new RPTTransaction();
    data.forEach(x => {
      item = new RPTTransaction();
      item.id = x.id;
      item.amount = x.id;
      item.drAcctNo = x.drAcctNo;
      item.crAcctNo = x.crAcctNo;
      item.serviceCode = x.serviceCode;
      item.transDescr = x.transDescr;
      item.systemDate = x.systemDate;
      item.successFlag = x.successFlag;
      exportTable.push(item);
    });
    return exportTable;
  }
}

