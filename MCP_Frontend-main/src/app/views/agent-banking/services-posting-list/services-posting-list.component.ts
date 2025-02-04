import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ServicePostingPolicy } from 'app/shared/models/service-posting-policy';
import { TransCode } from 'app/shared/models/trans-code';
import { AgentBankingService } from 'app/shared/services/agent-banking.service';
import { AlertService } from 'app/shared/services/alert.service';
import { RouterServiceService } from 'app/shared/services/router-service.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-services-posting-list',
  templateUrl: './services-posting-list.component.html',
  styleUrls: ['./services-posting-list.component.scss']
})
export class ServicesPostingListComponent implements OnInit {
  data: ServicePostingPolicy[];
  selectedData: ServicePostingPolicy;
  product: TransCode;
  loading = [false, false, false, false]

  constructor(private router: Router,
    private storage: StorageService,
    private alertService: AlertService,
    public dialogService: DialogService,
    public messageService: MessageService,
    private globalService: AgentBankingService,
    private navigation: RouterServiceService
  ) {

  }

  ngOnInit() {
    this.product = this.storage.getSelectedProduct();
    this.onSearch();
  }

  onSearch() {
    let request = new ServicePostingPolicy;
    request.serviceId = this.product.serviceId;
    this.loading[0] = true;
    this.globalService.findServicePostingPolicy(request)
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
    this.router.navigate(['agent-banking/service-posting-policy', 'Edit']);
  }

  onView() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to view');
      return;
    }
    this.storage.setEntity(this.selectedData);
    this.router.navigate(['agent-banking/service-posting-policy', 'View']);
  }


  
  onRemove() {
    if (this.selectedData == null) {
      this.alertService.displayInfo('Select a record to remove');
      return;
    }
    this.loading[0] = true;
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
        this.globalService.deleteServicePostingDetail(this.selectedData)
        .pipe(first())
        .subscribe(
          response => {
            this.loading[0] = false;
            if (response.code !== "00") {
              this.alertService.displayError(response.message);
              return;
            }
            this.onSearch();
          },
          error => {
            this.loading[0] = false;
            this.alertService.displayError(error);
          });
      },
      allowOutsideClick: () => !Swal.isLoading()
    })
    
  }


  onAdd() {
    this.router.navigate(['agent-banking/service-posting-policy', 'Add']);
  }

  onClose() {
    this.navigation.goBack();
  }

}
