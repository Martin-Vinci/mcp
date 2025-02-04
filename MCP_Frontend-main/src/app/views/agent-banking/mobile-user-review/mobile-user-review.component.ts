import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Customer } from 'app/shared/models/customer';
import { CustomerDetail } from 'app/shared/models/customer-details';
import { MobileUser } from 'app/shared/models/mobile-user';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { SecurityService } from 'app/shared/services/security.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { MobileUserReviewDetailsComponent } from '../mobile-user-review-details/mobile-user-review-details.component';

@Component({
  selector: 'app-mobile-user-review',
  templateUrl: './mobile-user-review.component.html',
  styleUrls: ['./mobile-user-review.component.scss']
})
export class MobileUserReviewComponent implements OnInit {
  customerType = [];
  data: MobileUser[];
  selectedData: MobileUser;
  loading = [false, false, false, false]
  ref: DynamicDialogRef;
  constructor(private alertService: AlertService,
    public dialogService: DialogService,
    public messageService: MessageService,
    private adminEndPoint: SecurityService,
    private globalService: AgentBankingService,
  ) {

  }

  ngOnInit() {
    this.onSearch();
  }

  onSearch() {
    let request = new MobileUser;
    this.loading[0] = true;
    this.data = null;
    this.globalService.findPendingCustomers(request)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          if (response.code !== "00") {
            //this.alertService.displayError(response.message);
            return;
          }
          this.data = response.data;
        },
        error => {
          this.loading[0] = false;
          this.alertService.displayError(error);
        });
  }

  reviewMobileUser(reviewAction: string) {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to modify');
      return;
    }


    Swal.fire({
      customClass: {
        confirmButton: 'btn btn-success',
        cancelButton: 'btn btn-danger'
      },
      showClass: {
        popup: 'animated flipInX'
      },
      buttonsStyling: true,
      icon: 'warning',
      title: "Are you sure ?",
      text: 'Please confirm action ?',
      inputAttributes: {
        autocapitalize: 'off'
      },
      showCancelButton: true,
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      showLoaderOnConfirm: true,
      preConfirm: () => {
        if (reviewAction == "Approve")
          this.loading[1] = true;
        else
          this.loading[2] = true;

        this.selectedData.reviewAction = reviewAction;
        this.selectedData.approvedBy = this.adminEndPoint.currentUser.userName;
        this.globalService.reviewMobileUser(this.selectedData)
          .pipe(first())
          .subscribe(
            response => {
              this.loading[1] = false;
              this.loading[2] = false;
              if (response.code !== "00") {
                this.alertService.displayError(response.message);
                return;
              }
              this.alertService.displaySuccess("Action submitted Successfully");
              this.onSearch();
            },
            () => {
              this.loading[2] = false;
            });
      },
      allowOutsideClick: () => !Swal.isLoading()
    })
    
  }

  
  async openDetailsWindow() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select customer record to view');
      return;
    }

    this.loading[1] = true;
    this.globalService.findCustomerDetails(this.selectedData)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[1] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          let transExport = response.data;
          this.ref = this.dialogService.open(MobileUserReviewDetailsComponent, {
            header: 'Customer Name [ ' + this.selectedData.customerName + ' ]',
            width: '70%',
            closable: true,
            contentStyle: { "max-height": "600px", "overflow": "auto" },
            ////baseZIndex: 10000,,
            data: transExport,
          });
          this.ref.onClose.subscribe((product: CustomerDetail) => {
            if (product) {
            }
          });
        },
        error => {
          this.loading[0] = false;
          this.alertService.displayError(error);
        });
  }
}
