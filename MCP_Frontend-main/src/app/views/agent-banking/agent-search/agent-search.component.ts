import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { AgentData } from 'app/shared/models/agent-data';
import { Charge } from 'app/shared/models/charge';
import { MobileUser } from 'app/shared/models/mobile-user';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { AgentbakingService } from 'app/shared/services/agentbaking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-agent-search',
  templateUrl: './agent-search.component.html',
  styleUrls: ['./agent-search.component.scss']
})
export class AgentSearchComponent implements OnInit {
  data: MobileUser[]; 
  selectedData: MobileUser;
  loading = [false, false, false, false]
  form: FormGroup;
  constructor(private router: Router,
    private storage: StorageService,
    private alertService: AlertService,
    private fb: FormBuilder,
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
    this.globalService.findAgents(this.form.value)
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
    this.router.navigate(['agent-banking/main-agent', 'Edit']);
  }

  onView() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to view');
      return;
    }
    this.storage.setSelectedProduct(this.selectedData);
    this.router.navigate(['agent-banking/main-agent', 'View']);
  }

  onAdd() {
    this.router.navigate(['agent-banking/main-agent', 'Add']);
  }

}
