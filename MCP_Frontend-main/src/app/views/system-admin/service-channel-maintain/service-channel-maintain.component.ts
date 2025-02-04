import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { DatePipeService } from 'app/shared/helpers/date-pipe.service';
import { ServiceChannel } from 'app/shared/models/biller';
import { AlertService } from 'app/shared/services/alert.service';
import { BillerService } from 'app/shared/services/biller.service';
import { RouterServiceService } from 'app/shared/services/router-service.service';
import { SecurityService } from 'app/shared/services/security.service';
import { StorageService } from 'app/shared/services/storage.service';
import { MenuItem } from 'primeng/api';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-service-channel-maintain',
  templateUrl: './service-channel-maintain.component.html',
  styleUrls: ['./service-channel-maintain.component.scss']
})
export class ServiceChannelMaintainComponent implements OnInit {
  loading = [false, false, false];
  submitted = false;
  data: ServiceChannel;
  customerClassId: number;
  customerType: string;
  isEdit = false;
  isView = false;
  isAddMode = false;
  cssBodyWidth: string = "col-10";
  items: MenuItem[];
  form: FormGroup;
  action: string;
  accountStructure = [];
  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private navigation: RouterServiceService,
    private securityService: SecurityService,
    private storageService: StorageService,
    private alertService: AlertService,
    private systemAdmin: BillerService,
    private dateService: DatePipeService
  ) {

  }

  ngOnInit() {
    this.action = this.route.snapshot.params['id'];
    this.createForms();
    this.prepareFormBasedOnAction();
    if (this.isAddMode == false) {
      this.findEntityData();
    }
  }

  ngOnDestroy() {
    //this.storageService.closeEntity();
  }

  prepareFormBasedOnAction() {
    if (this.action == "Add") {
      this.cssBodyWidth = "col-12";
      if (this.customerClassId != null)
        this.f.customerClassId.setValue(parseInt(this.customerClassId.toString()));
      this.isAddMode = !this.data;
    }
    else if (this.action == "View") {
      this.isView = true;
      this.data = this.storageService.getEntity();
      this.isAddMode = !this.data;
    }
    else if (this.action == "Edit") {
      this.isEdit = true;
      this.data = this.storageService.getEntity();
      this.isAddMode = !this.data;
    }
  }

  createForms() {
    this.form = this.fb.group({
      channelId: [null],
      channelCode: [null],
      description:[null],
      channelUsername:[null],
      channelPassword:[null],
      enforcePwdExpiry:[null],
      expiryDate: [null],
      status: [null],
      createdBy: [this.securityService.currentUser.userName],
      createDt: [this.securityService.currentUser.processDate],
      modifyBy: [this.securityService.currentUser.userName],
      modifyDt: [this.securityService.currentUser.processDate],
    });
  }

  get f() {
    return this.form.controls;
  }

  validation_messages = {
   
  };


  findEntityData() {
    this.f.channelId.setValue(this.data.channelId);
    this.f.channelCode.setValue(this.data.channelCode.trim());
    this.f.description.setValue(this.data.description.trim());
    this.f.channelUsername.setValue(this.data.channelUsername.trim());
    this.f.channelPassword.setValue(this.data.channelPassword.trim());
    this.f.enforcePwdExpiry.setValue(this.data.enforcePwdExpiry.trim());
    this.f.expiryDate.setValue(this.data.expiryDate);
    this.f.status.setValue(this.data.status.trim());
    this.f.createDt.setValue(this.data.createDt);
  }


  onSubmit() {
    this.submitted = true;
    if (this.form.invalid) {
      this.alertService.displayInfo("Please fill all the fields");
      this.loading[0] = false;
      return;
    }
    this.loading[0] = true;
    this.systemAdmin.maintainServiceChannel(this.prepareSubmitData())
      .pipe(first())
      .subscribe(
        response => {
          this.loading[0] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.alertService.displaySuccess("Record Saved Successfully");
          this.submitted = false;
          this.onClose();
        },
        error => {
          this.loading[0] = false;
          this.submitted = false;
          this.alertService.displayError(error);
        });
  }

  onKeyUP(inPutValue: string): void {
    this.f.channelCode.setValue(inPutValue.toUpperCase());
  }


  prepareSubmitData(): ServiceChannel {
    let request = new ServiceChannel;
    request.channelId = this.f.channelId.value;
    request.channelCode = this.f.channelCode.value;
    request.description = this.f.description.value;
    request.channelUsername = this.f.channelUsername.value;
    request.channelPassword = this.f.channelPassword.value;
    request.enforcePwdExpiry = this.f.enforcePwdExpiry.value;
    request.expiryDate = this.dateService.isoDateString(this.f.expiryDate.value);
    request.status = this.f.status.value;
    request.createdBy = this.f.createdBy.value;
    request.createDt = this.f.createDt.value;
    return request;
  }

  onClose() {
    this.navigation.goBack();
  }
}
