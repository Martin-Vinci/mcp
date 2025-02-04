import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { TransactionDetails } from 'app/shared/models/transaction-details';
import { TransactionRef, TransData } from 'app/shared/models/transaction-ref';
import * as moment from 'moment';
import { MessageService } from 'primeng/api';
import { DialogService, DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

@Component({
  selector: 'app-transactions-details',
  templateUrl: './transactions-details.component.html',
  styleUrls: ['./transactions-details.component.scss']
})
export class TransactionsDetailsComponent implements OnInit {
  isTableLoading = false;
  form: FormGroup;  
  loading = false;
  errorMessage: string;
  data: TransactionDetails[];
  selectedData: TransactionRef;
  public value: moment.Moment;
  constructor(
    private fb: FormBuilder,
    public dialogService: DialogService,
    public messageService: MessageService,
    public ref: DynamicDialogRef,
    public config: DynamicDialogConfig,
  ) {

  }

  ngOnInit() {
    let transData = new TransData;
    transData = this.config.data;
    this.selectedData = transData.mainTrans;
    this.data = transData.childTrans;
    this.createForms();
  }

  createForms() {
    this.form = this.fb.group({
      crAcctNo: [this.selectedData.crAcctNo],
      drAcctNo: [this.selectedData.drAcctNo],
      amount: [this.selectedData.amount],
      postedBy: [this.selectedData.postedBy],
      serviceCode: [this.selectedData.serviceCode],
      transDescr: [this.selectedData.transDescr],
      successFlag: [this.selectedData.successFlag],
      transDate: [this.selectedData.transDate],
      utilPosted: [this.selectedData.utilPosted],
      reversalFlag: [this.selectedData.reversalFlag],
      reversalReason: [this.selectedData.reversalReason],
      depositorPhone: [this.selectedData.depositorPhone],
      depositorName: [this.selectedData.depositorName],
      systemDate: [this.selectedData.systemDate],
      initiatorPhoneNo: [this.selectedData.initiatorPhoneNo]
    });
  }

  cancel() {
    this.ref.close(false);
  }
}
