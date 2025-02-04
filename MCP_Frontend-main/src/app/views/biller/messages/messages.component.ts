import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { Biller } from 'app/shared/models/biller';
import { MessageBox } from 'app/shared/models/message-box';
import { SearchCriteria } from 'app/shared/models/search-criteria';
import { AlertService } from 'app/shared/services/alert.service';
import { BillerService } from 'app/shared/services/biller.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-messages',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.scss']
})
export class MessagesComponent implements OnInit {
  data: MessageBox[];
  selectedData: MessageBox;
  loading = [false, false, false, false]
  form: FormGroup;
  constructor(private router: Router,
    private storage: StorageService,
    private fb: FormBuilder,
    private alertService: AlertService,
    public dialogService: DialogService,
    public messageService: MessageService,
    private globalService: BillerService,
  ) {

  }

  getDate(): string {
    var today = new Date();
    var dd = String(today.getDate()).padStart(2, '0');
    var mm = String(today.getMonth() + 1).padStart(2, '0'); //January is 0!
    var yyyy = today.getFullYear();
   return yyyy + '-' + mm + '-' + dd;
  }

  ngOnInit() {
    this.createForms();
    this.onSearch();
  }


  createForms() {
    this.form = this.fb.group({
      fromDate: [this.getDate()],
      toDate: [this.getDate()],
      phoneNo: [null]
    });
  }


  onSearch() {
    if (this.form.invalid)
      return;

    this.loading[0] = true;
    this.globalService.findAllSMS(this.form.value)
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


  onEdit() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to modify');
      return;
    }
    this.storage.setEntity(this.selectedData);
    this.router.navigate(['biller-control/biller', 'Edit']);
  }

  onView() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to view');
      return;
    }
    this.storage.setEntity(this.selectedData);
    this.router.navigate(['biller-control/biller', 'View']);
  }

  onAdd() {
    this.router.navigate(['biller-control/biller', 'Add']);
  }

}
