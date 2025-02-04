import { Component, OnInit } from "@angular/core";
import { FormGroup, FormBuilder, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { Customer } from "app/shared/models/customer";
import { AlertService } from "app/shared/services/alert.service";
import { CustomerService } from "app/shared/services/customer-service.service";
import { StorageService } from "app/shared/services/storage.service";
import { MenuItem } from "primeng/api";
import { first } from "rxjs/operators";


@Component({
  selector: 'app-customer-maintain',
  templateUrl: './customer-maintain.component.html',
  styleUrls: ['./customer-maintain.component.scss']
})
export class CustomerMaintainComponent implements OnInit {
  loading = false;
  submitted = false;
  selectedData: Customer;
  isEdit = false;
  isView = false;
  isAddMode = false;
  form: FormGroup;
  recordId: number;
  action: string;
  items: MenuItem[];
  statusPickList = [];
  categories = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private alertService: AlertService,
    private fb: FormBuilder,
    private customerAPI: CustomerService,
    private storageService: StorageService,
  ) {

  }

  ngOnInit() {
    this.action = this.route.snapshot.params['id'];
    this.initializeSubMenus();
    this.prepareFormBasedOnAction();
    this.createForm();
    if (this.isAddMode == false) {
      this.findEntityData();
    }
  }

  ngOnDestroy() {
   // this.storageService.closeEntity();
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

  initializeSubMenus() {
    this.items = [{
      label: 'Menu',
      items: [{
        label: 'Default Products',
        icon: 'pi pi-search',
        routerLink: '/customer/customer-address-search'
      },
      {
        label: 'Contact Mode',
        icon: 'pi pi-phone',
        routerLink: '/customer/customer-contact-search'
      }
      ]
    }
    ];
  }

  createForm() {
    this.form = this.fb.group({
      id: null,
      firstName: [null, Validators.required],
      middleName: null,
      lastName: [null, Validators.required],
      title: [null, Validators.required],
      status: ['Active', Validators.required],
      occupation: [null, Validators.required],
      emailAddress: null,
      latitudes: null,
      longitudes: null,
      rowVersion: 0,
      birthDt: [null, Validators.required],
      placeOfResidence: [null, Validators.required],
      fcsNo: null,
      maritalStatus: [null, Validators.required],
      businessType: [null, Validators.required],
      idType: null,
      idNo: [null, Validators.required],
      nextOfKinName: [null, Validators.required],
      nextOfKinAddress: [null, Validators.required],
      gps: null,
      phoneNo: [null, Validators.compose([Validators.required, Validators.pattern(/^(\d{12}|\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3}))$/)])],
      createDate: "2022-01-01",
      createBy: "GP0001",
      modifiedDate: null,
      modifiedBy: null,
      password: null,
      edit: false
    });
  }

  get f() {
    return this.form.controls;
  }

  validation_messages = {
    firstName: [{ type: 'required', message: 'First Name is required' }],
    lastName: [{ type: 'required', message: 'Last Name is required' }],
    title: [{ type: 'required', message: 'Title is required' }],
    status: [{ type: 'required', message: 'Status is required' }],
    occupation: [{ type: 'required', message: 'Occupation is required' }],
    birthDt: [{ type: 'required', message: 'Birth date is required' }],
    placeOfResidence: [{ type: 'required', message: 'Place of residence is required' }],
    maritalStatus: [{ type: 'required', message: 'Marital status is required' }],
    businessType: [{ type: 'required', message: 'Business type is required' }],
    nextOfKinName: [{ type: 'required', message: 'Next of Kin name is required' }],
    nextOfKinAddress: [{ type: 'required', message: 'Next of Kin Address is required' }],
    phoneNo: [{ type: 'required', message: 'Phone number is required' }],
    idNo: [{ type: 'required', message: 'ID number is required' }],
  };


  findEntityData() {
    this.f.id.setValue(this.selectedData.id);
    this.f.firstName.setValue(this.selectedData.firstName.trim());
    this.f.middleName.setValue(this.selectedData.middleName);
    this.f.lastName.setValue(this.selectedData.lastName.trim());
    this.f.title.setValue(this.selectedData.title.trim());
    this.f.status.setValue(this.selectedData.status.trim());
    this.f.occupation.setValue(this.selectedData.occupation.trim());
    this.f.emailAddress.setValue(this.selectedData.emailAddress.trim());
    this.f.latitudes.setValue(this.selectedData.latitudes);
    this.f.longitudes.setValue(this.selectedData.longitudes);
    this.f.rowVersion.setValue(this.selectedData.rowVersion);
    this.f.birthDt.setValue(this.selectedData.birthDt);
    this.f.placeOfResidence.setValue(this.selectedData.placeOfResidence.trim());
    this.f.fcsNo.setValue(this.selectedData.fcsNo);
    this.f.maritalStatus.setValue(this.selectedData.maritalStatus.trim());
    this.f.businessType.setValue(this.selectedData.businessType.trim());
    this.f.idType.setValue(this.selectedData.idType);
    this.f.idNo.setValue(this.selectedData.idNo.trim());
    this.f.nextOfKinName.setValue(this.selectedData.nextOfKinName);
    this.f.nextOfKinAddress.setValue(this.selectedData.nextOfKinAddress);
    this.f.gps.setValue(this.selectedData.gps);
    this.f.phoneNo.setValue(this.selectedData.phoneNo.trim());
    this.f.createBy.setValue(this.selectedData.createBy);
    this.f.createDate.setValue(this.selectedData.createDate);
    this.f.password.setValue(this.selectedData.password);
    this.f.edit.setValue(true);
  }


  onSubmit() {
    this.submitted = true;
    if (this.form.invalid) {
      this.alertService.displayInfo("Please fill all the fields");
      this.loading = false;
      return;
    }
    this.loading = true;
    this.customerAPI.signUp(this.prepareSubmitData())
      .pipe(first())
      .subscribe(
        response => {
          this.loading = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.alertService.displaySuccess("Record Saved Successfully");
          this.submitted = false;
          this.onClose();
        },
        error => {
          this.loading = false;
          this.submitted = false;
          this.alertService.displayError(error);
        });
  }

  prepareSubmitData(): Customer {
    let request = new Customer;
    request.id = this.f.id.value;
    request.firstName = this.f.firstName.value;
    request.middleName = this.f.middleName.value;
    request.lastName = this.f.lastName.value;
    request.title = this.f.title.value;
    request.status = this.f.status.value;
    request.occupation = this.f.occupation.value;
    request.emailAddress = this.f.emailAddress.value;
    request.latitudes = this.f.latitudes.value;
    request.longitudes = this.f.longitudes.value;
    request.rowVersion = this.f.rowVersion.value;
    request.birthDt = this.f.birthDt.value;
    request.placeOfResidence = this.f.placeOfResidence.value;
    request.fcsNo = this.f.fcsNo.value;
    request.maritalStatus = this.f.maritalStatus.value;
    request.businessType = this.f.businessType.value;
    request.idType = this.f.idType.value;
    request.idNo = this.f.idNo.value;
    request.nextOfKinName = this.f.nextOfKinName.value;
    request.nextOfKinAddress = this.f.nextOfKinAddress.value;
    request.gps = this.f.gps.value;
    request.phoneNo = this.f.phoneNo.value;
    request.createBy = this.f.createBy.value;
    request.createDate = this.f.createDate.value;
    request.password = this.f.password.value;
    request.edit = this.f.edit.value;
    return request;
  }

  onClose() {
    this.router.navigate(['webcash/customer-search']);
  }

}
