import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { TransCode } from 'app/shared/models/trans-code';
import { AlertService } from 'app/shared/services/alert.service';
import { BillerService } from 'app/shared/services/biller.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';
import * as FileSaver from 'file-saver';
import { Biller, ServiceChannel } from 'app/shared/models/biller';

@Component({
  selector: 'app-notifications-search',
  templateUrl: './notifications-search.component.html',
  styleUrls: ['./notifications-search.component.scss']
})
export class NotificationsSearchComponent implements OnInit {
  customerType = [];
  data: TransCode[];
  selectedData: TransCode;

  billerList: Biller[];
  serviceChannelList: ServiceChannel[];

  form: FormGroup;
  loading = [false, false, false, false]

  constructor(private router: Router,
    private storage: StorageService,
    private fb: FormBuilder,
    private alertService: AlertService,
    public dialogService: DialogService,
    public messageService: MessageService,
    private billerService: BillerService,
  ) {

  }

  ngOnInit() {
    this.createForms();
    this.findBillers();
    this.findChannels();
    this.onSearch();
  }

  createForms() {
    this.form = this.fb.group({
      startDate: [this.getDate()],
      endDate: [this.getDate()],
      billerCode: [null],
      channelCode: [null]
    });
  }

  getDate(): string {
    var today = new Date();
    var dd = String(today.getDate()).padStart(2, '0');
    var mm = String(today.getMonth() + 1).padStart(2, '0'); //January is 0!
    var yyyy = today.getFullYear();
    return yyyy + '-' + mm + '-' + dd;
  }


  onSearch() {
    this.loading[0] = true;
    this.data = [];
    this.billerService.findNotifications(this.form.value)
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


  findBillers() {
    let request = new Biller;
    this.billerService.findBillers(request)
      .pipe(first())
      .subscribe(
        response => {
          if (response.code !== "00") {
            return;
          }
          this.billerList = response.data;

          this.billerList.sort(this.dynamicSort("description"))
        },
        error => {
          this.alertService.displayError(error);
        });
  }

  findChannels() {
    let request = new ServiceChannel;
    this.billerService.findChannels(request)
      .pipe(first())
      .subscribe(
        response => {
          if (response.code !== "00") {
            return;
          }
          this.serviceChannelList = response.data;

          this.serviceChannelList.sort(this.dynamicSort("description"))
        },
        error => {
          this.alertService.displayError(error);
        });
  }



  dynamicSort(property) {
    var sortOrder = 1;
    if (property[0] === "-") {
      sortOrder = -1;
      property = property.substr(1);
    }
    return function (a, b) {
      /* next line works with strings and numbers, 
       * and you may want to customize it to your needs
       */
      var result = (a[property] < b[property]) ? -1 : (a[property] > b[property]) ? 1 : 0;
      return result * sortOrder;
    }
  }



  exportExcel() {
    import("xlsx").then(xlsx => {
      const worksheet = xlsx.utils.json_to_sheet(this.data);
      const workbook = { Sheets: { data: worksheet }, SheetNames: ["data"] };
      const excelBuffer: any = xlsx.write(workbook, {
        bookType: "xlsx",
        type: "array"
      });
      this.saveAsExcelFile(excelBuffer, "Biller Notifications-List");
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
