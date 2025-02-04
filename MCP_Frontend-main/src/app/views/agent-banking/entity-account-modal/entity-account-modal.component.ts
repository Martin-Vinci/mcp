import { SelectionModel } from '@angular/cdk/collections';
import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { MatTableDataSource } from '@angular/material/table';
import { ChargeData } from 'app/shared/models/charge-data';
import { ChargeTier, ChargeTierData } from 'app/shared/models/charge-tier-data';
import { MobileUser } from 'app/shared/models/mobile-user';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { SecurityService } from 'app/shared/services/security.service';
import * as moment from 'moment';
import { MessageService } from 'primeng/api';
import { DialogService, DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-entity-account-modal',
  templateUrl: './entity-account-modal.component.html',
  styleUrls: ['./entity-account-modal.component.scss']
})
export class EntityAccountModalComponent implements OnInit {
  isTableLoading = false;
  form: FormGroup;
  loading = false;
  errorMessage: string;
  rightsArray: ChargeTier[] = [];
  selectedUser = new MobileUser();
  public dataSource = new MatTableDataSource<ChargeTier>();
  public displayedColumns = ['tierNo', 'fromAmt', 'toAmt', 'chargeAmt', 'Remove'];
  selection = new SelectionModel<ChargeTier>(false, []);
  public value: moment.Moment;
  constructor(
    private agentBankingService: AgentBankingService,
    private securityService: SecurityService,
    private formBuilder: FormBuilder,
    public dialogService: DialogService,
    public messageService: MessageService,
    public ref: DynamicDialogRef,
    public config: DynamicDialogConfig,
  ) {

  }

  ngOnInit() {
    this.selectedUser = this.config.data;
    this.setForm();
  }

  private setForm() {
    this.form = this.formBuilder.group({
      id: null,
      dateAdded: null,
      mobileUserId: this.selectedUser.id,
      acctNo: [null, Validators.required],
      description: this.selectedUser.customerName,
      active: true,
      acctType: this.selectedUser.accountsCategory
    });
  }

  get f() {
    return this.form.controls;
  }


  async onSubmit() {
    this.errorMessage = "";
    if (this.form.invalid) {
      this.errorMessage = "Please fill all the fields";
      return;
    }

    this.loading = true;
    this.agentBankingService.saveUserAccount(this.form.value)
      .pipe(first())
      .subscribe(
        response => {
          this.loading = false;
          if (response.code !== "00") {
            this.errorMessage = response.message;
            return;
          }
          this.loading = false;
          this.ref.close(this.form.value);
        },
        error => {
          this.errorMessage = error;
          this.loading = false;
        });
  }

  cancel() {
    this.ref.close(false);
  }
}
