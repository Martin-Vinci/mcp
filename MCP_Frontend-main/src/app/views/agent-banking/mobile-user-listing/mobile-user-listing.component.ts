import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { Customer } from 'app/shared/models/customer';
import { MobileUser } from 'app/shared/models/mobile-user';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-mobile-user-listing',
  templateUrl: './mobile-user-listing.component.html',
  styleUrls: ['./mobile-user-listing.component.scss']
})
export class MobileUserListingComponent implements OnInit {

  customerType = [];
  data: MobileUser[];
  selectedData: MobileUser;
  loading = [false, false, false, false]
  form: FormGroup;
  constructor(private router: Router,
    private storage: StorageService,
    private fb: FormBuilder,
    private alertService: AlertService,
    public dialogService: DialogService,
    public messageService: MessageService,
    private globalService: AgentBankingService,
  ) {

  }

  ngOnInit() {
    this.createForms();
  }

  createForms() {
    this.form = this.fb.group({
      phoneNumber: [null],
      customerName: [null],
    });
  }

  get f() {
    return this.form.controls;
  }

  onSearch() {
    this.loading[0] = true;
    this.f.phoneNumber.setValue(this.f.phoneNumber.value == "" ? null : this.f.phoneNumber.value);
    this.f.customerName.setValue(this.f.customerName.value == "" ? null : this.f.customerName.value);
    this.globalService.findMobileUser(this.form.value)
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
    this.storage.setSelectedProduct(this.selectedData);
    this.router.navigate(['agent-banking/mobile-user-maintain', 'Edit']);
  }

  onView() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to view');
      return;
    }
    this.storage.setSelectedProduct(this.selectedData);
    this.router.navigate(['agent-banking/mobile-user-maintain', 'View']);
  }

  onAdd() {
    this.router.navigate(['agent-banking/mobile-user-maintain', 'Add']);
  }
}
