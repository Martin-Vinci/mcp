import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { DomSanitizer } from '@angular/platform-browser';
import { DatePipeService } from 'app/shared/helpers/date-pipe.service';
import { CustomerDetail } from 'app/shared/models/customer-details';
import { TransactionDetails } from 'app/shared/models/transaction-details';
import { TransactionRef, TransData } from 'app/shared/models/transaction-ref';
import * as moment from 'moment';
import { MessageService } from 'primeng/api';
import { DialogService, DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

@Component({
  selector: 'app-mobile-user-review-details',
  templateUrl: './mobile-user-review-details.component.html',
  styleUrls: ['./mobile-user-review-details.component.scss']
})
export class MobileUserReviewDetailsComponent implements OnInit {
  isTableLoading = false;
  form: FormGroup;
  loading = false;
  errorMessage: string;
  x: CustomerDetail;
  public value: moment.Moment;

  photoImage: any;
  signatureImage: any;

  constructor(
    private fb: FormBuilder,
    public dialogService: DialogService,
    public messageService: MessageService,
    private datePipe: DatePipeService,
    public ref: DynamicDialogRef,
    public config: DynamicDialogConfig,
    private sanitizer: DomSanitizer,
  ) {

  }

  ngOnInit() {
    this.x = this.config.data;
    console.log(this.x);
    this.generatePhoto(this.x.photoBase64String, this.x.signatureBase64String);
    this.createForms();
  }

  createForms() {
    this.form = this.fb.group({
      firstName: [this.x.firstName],
      surName: [this.x.surName],
      dateOfBirth: [this.datePipe.isoDateString(this.x.dateOfBirth)],
      town: [this.x.town],
      idNumber: [this.x.idNumber],
      mobilePhone: [this.x.mobilePhone],
      idExpiryDate: [this.datePipe.isoDateString(this.x.idExpiryDate)],
      idIssueDt: [this.datePipe.isoDateString(this.x.idIssueDt)],
      gender: [this.x.gender],
      outletNo: [this.x.outletNo],
      nokName: [this.x.nokName],
      nokPhone: [this.x.nokPhone],
    });
  }


  generatePhoto(photo: string, signature: string) {
    if (photo != null) {
      let photoURL = 'data:image/jpeg;base64,' + photo;
      this.photoImage = this.sanitizer.bypassSecurityTrustUrl(photoURL);
    }
    if (signature != null) {
      let photoURL = 'data:image/jpeg;base64,' + signature;
      this.signatureImage = this.sanitizer.bypassSecurityTrustUrl(photoURL);
    }
  }

  cancel() {
    this.ref.close(false);
  }
}
