import { SelectionModel } from '@angular/cdk/collections';
import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { Charge } from 'app/shared/models/charge';
import { ChargeData } from 'app/shared/models/charge-data';
import { ChargeTier, ChargeTierData } from 'app/shared/models/charge-tier-data';
import { CommissionData } from 'app/shared/models/commission-data';
import { CommissionTier, CommissionTierData } from 'app/shared/models/commission-tier-data';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { AgentbakingService } from 'app/shared/services/agentbaking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { SecurityService } from 'app/shared/services/security.service';
import { StorageService } from 'app/shared/services/storage.service';
import * as moment from 'moment';
import { MessageService } from 'primeng/api';
import { DialogService, DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-services-commission-tiers',
  templateUrl: './services-commission-tiers.component.html',
  styleUrls: ['./services-commission-tiers.component.scss']
})
export class ServicesCommissionTiersComponent implements OnInit {
  isTableLoading = false;
  form: FormGroup;
  loading = false;
  errorMessage: string;
  rightsArray: CommissionTier[] = [];
  selectedCharge = new CommissionData();
  public dataSource = new MatTableDataSource<CommissionTier>();
  public displayedColumns = ['tierNo', 'fromAmt', 'toAmt', 'commissionAmount', 'Remove'];
  selection = new SelectionModel<CommissionTier>(false, []);
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
    this.setForm();
    this.selectedCharge = this.config.data;
    console.log(this.selectedCharge);
    this.findTieres();
  }

  private setForm() {
    this.form = this.formBuilder.group({
      toAmount: null,
      tierAmount: null
    });
  }

  get f() {
    return this.form.controls;
  }


  findTieres() {
    this.isTableLoading = true;
    let search = new CommissionTier();
    search.commissionId = this.selectedCharge.commissionId;
    //this.loading = true;
    this.agentBankingService.findServiceCommissionTiers(search)
      .pipe(first())
      .subscribe(
        response => {
          this.loading = false;
          if (response.code !== "00") {
            return;
          }
          this.dataSource.data = response.data as CommissionTier[];
          this.rightsArray = this.dataSource.data;
        },
        error => {
          this.loading = false;
        });
  }

  async maintainServiceChargeTiers() {
    this.errorMessage = "";
    this.loading = true;
    let request = new CommissionTierData;
    request.data = this.dataSource.data;
    request.createdBy = this.securityService.currentUser.userName;
    request.createDate = this.securityService.currentUser.processDate;
    request.commissionId = this.selectedCharge.commissionId;

    this.agentBankingService.maintainServiceCommissionTiers(request)
      .pipe(first())
      .subscribe(
        response => {
          this.loading = false;
          if (response.code !== "00") {
            this.errorMessage = response.message;
            return;
          }
          this.loading = false;
          this.cancel();
        },
        error => {
          this.errorMessage = error;
          this.loading = false;
        });
  }

  onToAmountEdited(value, rowIndex: number) {
    this.rightsArray[rowIndex].toAmt = value;
    this.dataSource.data = this.rightsArray as CommissionTier[];
  }
  onTierAmountEdited(value, rowIndex: number) {
    this.rightsArray[rowIndex].commissionAmount = value;
    this.dataSource.data = this.rightsArray as CommissionTier[];
  }


  addRow() {
    this.errorMessage = "";
    let lastRow = new CommissionTier();
    let item = new CommissionTier();
    if (this.rightsArray == null || this.rightsArray.length == 0) {
      this.rightsArray = [];
      item.tierNo = 1;
      item.fromAmt = 1;
    } else {
      lastRow = this.rightsArray[this.rightsArray.length - 1];
      if (lastRow.toAmt == null || lastRow.toAmt == 0 || lastRow.commissionAmount == null || lastRow.commissionAmount == 0) {
        this.errorMessage = "To Amount and Tier Amount cannot be empty";
        return;
      }
      if (lastRow.fromAmt > lastRow.toAmt) {
        this.errorMessage = "To Amount should be greater than from amount";
        return;
      }
      item.tierNo = +lastRow.tierNo + +1;
      item.fromAmt = +lastRow.toAmt + +1;
    }
    item.createdBy = this.securityService.currentUser.userName;
    this.rightsArray.push(item);
    this.dataSource.data = this.rightsArray as CommissionTier[];
  }

  removeRow(rowIndex: number) {
    let count = 1;
    this.rightsArray.splice(rowIndex, 1);
    this.rightsArray.forEach(element => {
      element.tierNo = count;
      count++;
    });
    this.dataSource.data = this.rightsArray as CommissionTier[];
  }


  cancel() {
    this.ref.close(false);
  }
}
