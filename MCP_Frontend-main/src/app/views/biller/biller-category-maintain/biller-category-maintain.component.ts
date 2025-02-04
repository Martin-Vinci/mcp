import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DatePipeService } from 'app/shared/helpers/date-pipe.service';
import { Biller } from 'app/shared/models/biller';
import { BillerProduct } from 'app/shared/models/biller-products';
import { BillerCategory } from 'app/shared/models/BillerCategory';
import { PostingPolicyItem } from 'app/shared/models/service-posting-policy';
import { AlertService } from 'app/shared/services/alert.service';
import { BillerService } from 'app/shared/services/biller.service';
import { RouterServiceService } from 'app/shared/services/router-service.service';
import { SecurityService } from 'app/shared/services/security.service';
import { StorageService } from 'app/shared/services/storage.service';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-biller-category-maintain',
  templateUrl: './biller-category-maintain.component.html',
  styleUrls: ['./biller-category-maintain.component.scss']
})
export class BillerCategoryMaintainComponent implements OnInit {
  loading = [false, false, false];
  submitted = false;
  selectedData: BillerCategory;
  product: Biller;
  isEdit = false;
  isView = false;
  isAddMode = false;
  segDataPickList = [];
  form: FormGroup;
  recordId: number;
  action: string;
  bankShareReadOnly = true;
  vendorShareReadOnly = true;
  agentShareReadOnly = true;
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private alertService: AlertService,
    private fb: FormBuilder,
    private datePipe: DatePipeService,
    private navigation: RouterServiceService,
    private securityService: SecurityService,
    private agentBanking: BillerService,
    private storageService: StorageService,
  ) {

  }

  ngOnInit() {
    this.action = this.route.snapshot.params['id'];
    this.product = this.storageService.getSelectedProduct();
    this.prepareFormBasedOnAction();
    this.createForms();
    if (this.isAddMode == false) {
      this.findEntityData();
    }
  }

  ngOnDestroy() {
    //this.storageService.closeEntity();
  }

  prepareFormBasedOnAction() {
    if (this.action == "Add") {
      this.isAddMode = true;
    }
    else if (this.action == "View") {
      this.isView = true;
      this.selectedData = this.storageService.getEntity();
      this.isAddMode = !this.selectedData;
    }
    else if (this.action == "Edit") {
      this.isEdit = true;
      this.selectedData = this.storageService.getEntity();
      this.isAddMode = !this.selectedData;
    }
  }


  createForms() {
    this.form = this.fb.group({
      billerProductCategoryId: [null],
      description: [null],
      billerId: [this.product.id],
      status: [null],
      createdBy: [this.securityService.currentUser.userName],
      createDate: [this.securityService.currentUser.processDate],
      modifiedBy: [this.securityService.currentUser.userName],
      modifyDate: [this.securityService.currentUser.processDate],
      edit: false
    });
  }


  get f() {
    return this.form.controls;
  }


  validation_messages = {
    position: [{ type: 'required', message: 'Position is required' }],
    description: [{ type: 'required', message: 'Description is required' }],
    dataTypeCode: [{ type: 'required', message: 'Category is required' }],
    segDataId: [{ type: 'required', message: 'Segment data is required' }],
  };


  findEntityData() {
    this.f.billerProductCategoryId.setValue(this.selectedData.billerProductCategoryId);
    this.f.description.setValue(this.selectedData.description);
    this.f.billerId.setValue(this.selectedData.billerId);
    this.f.status.setValue(this.selectedData.status);
    this.f.createdBy.setValue(this.selectedData.createdBy);
    this.f.createDate.setValue(this.datePipe.isoDateString(this.selectedData.createDate));
    this.f.edit.setValue(true);
  }


  onSubmit() {
    this.submitted = true;
    if (this.form.invalid) {
      this.alertService.displayInfo("Please fill all the fields");
      this.loading[0] = false;
      return;
    }
    this.loading[0] = true;
    this.agentBanking.maintainBillerProductCategory(this.form.value)
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
          console.log(JSON.stringify(error));
          this.loading[0] = false;
          this.submitted = false;
          this.alertService.displayError('Error ' + error.status + ": " + error.error.error);
        });
  }

  onClose() {
    this.navigation.goBack();
  }

}
