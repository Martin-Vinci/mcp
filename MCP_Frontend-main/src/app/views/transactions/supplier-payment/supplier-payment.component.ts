import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { BuyerDetails, GoodsDetail, InvoiceData, Summary, TaxDetail } from 'app/shared/models/efris-invoice-data';
import { GoodsData } from 'app/shared/models/goods-data';
import { TaxPayer } from 'app/shared/models/tax-payer';
import { AlertService } from 'app/shared/services/alert.service';
import { EfrisService } from 'app/shared/services/efris.service';
import { RouterServiceService } from 'app/shared/services/router-service.service';
import { SecurityService } from 'app/shared/services/security.service';
import { StorageService } from 'app/shared/services/storage.service';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';
import { ModalEfrisGoodsAndServiceComponent } from '../modal-efris-goods-and-service/modal-efris-goods-and-service.component';
import { SupplierGoodsAndServicesComponent } from '../supplier-goods-and-services/supplier-goods-and-services.component';


@Component({
  selector: 'app-supplier-payment',
  templateUrl: './supplier-payment.component.html',
  styleUrls: ['./supplier-payment.component.scss'],
})
export class SupplierPaymentComponent implements OnInit {
  loading = [false, false, false];
  submitted = false;
  selectedData = new BuyerDetails;
  invoiceSummary = new Summary();
  isEdit = false;
  isView = false;
  isSuperAgent = false;
  canAddAcct = false;
  isAddMode = false;
  cssBodyWidth: string = "col-10";
  form: FormGroup;
  action: string;
  goodDetails = [];
  selectedGood: GoodsDetail;
  taxDetails = [];
  selectedTax: TaxDetail;
  ref: DynamicDialogRef;
  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private navigation: RouterServiceService,
    private storageService: StorageService,
    private alertService: AlertService,
    public dialogService: DialogService,
    private securityService: SecurityService,
    private efrisService: EfrisService
  ) {

  }

  ngOnInit() {
    this.action = this.route.snapshot.params['id'];
    this.createForms();
    this.updateInvoiceSummary(null);
    //this.prepareFormBasedOnAction();
    if (this.isAddMode == false) {
      //this.findEntityData();
    }
  }

  ngOnDestroy() {
    ///sessionStorage.removeItem('GoodsDetails');
  }


  prepareFormBasedOnAction() {
    if (this.action == "Add") {
      this.isAddMode = !this.selectedData;
    }
    else if (this.action == "View") {
      this.isView = true;
      this.selectedData = this.storageService.getSelectedProduct();
      this.isAddMode = !this.selectedData;
      //this.getAccounts();
    }
  }


  // getGoodsAndServices() {
  //   let request = new MobileUserAcct;
  //   //request.entityId = this.selectedData.phoneNumber;
  //   request.mobileUserId = this.selectedData.id;
  //   this.loading[1] = true;
  //   this.efrisService.findMobileUserAccounts(request)
  //     .pipe(first())
  //     .subscribe(
  //       response => {
  //         this.loading[1] = false;
  //         if (response.code !== "00") {
  //           return;
  //         }
  //         this.data = response.data;
  //         this.data.forEach(element => {
  //           if (element.acctType.trim() == "COMMISSION")
  //             this.canAddAcct = false;
  //         });

  //       },
  //       error => {
  //         this.loading[1] = false;
  //         this.alertService.displayError(error);
  //       });
  // }

  createForms() {
    this.form = this.fb.group({
      buyerTin: [null, Validators.required],
      buyerNinBrn: null,
      buyerPassportNum: null,
      buyerLegalName: [null, Validators.required],
      buyerBusinessName: null,
      buyerAddress: null,
      buyerEmail: null,
      buyerMobilePhone: null,
      buyerLinePhone: null,
      buyerPlaceOfBusi: null,
      buyerType: "0",
      buyerCitizenship: "0",
      buyerSector: null,
      buyerReferenceNo: null,
      remarks: null,
      edit: false
    });
  }

  get f() {
    return this.form.controls;
  }

  findEntityData() {
    try {
      this.f.buyerTin.setValue(this.selectedData.buyerTin);
      this.f.buyerNinBrn.setValue(this.selectedData.buyerNinBrn);
      this.f.buyerPassportNum.setValue(this.selectedData.buyerPassportNum);
      this.f.buyerLegalName.setValue(this.selectedData.buyerLegalName);
      this.f.buyerBusinessName.setValue(this.selectedData.buyerBusinessName);
      this.f.buyerAddress.setValue(this.selectedData.buyerAddress);
      this.f.buyerEmail.setValue(this.selectedData.buyerEmail);
      this.f.buyerMobilePhone.setValue(this.selectedData.buyerMobilePhone);
      this.f.buyerLinePhone.setValue(this.selectedData.buyerLinePhone);
      this.f.buyerPlaceOfBusi.setValue(this.selectedData.buyerPlaceOfBusi);
      this.f.buyerType.setValue(this.selectedData.buyerType);
      this.f.buyerCitizenship.setValue(this.selectedData.buyerCitizenship);
      this.f.buyerSector.setValue(this.selectedData.buyerSector);
      this.f.buyerReferenceNo.setValue(this.selectedData.buyerReferenceNo);
    }
    catch (e) {
      console.log(e);
    }
  }


  onSubmit() {
    this.submitted = true;
    if (this.form.invalid) {
      this.alertService.displayInfo("Please fill all the fields");
      this.loading[0] = false;
      return;
    }
    this.loading[0] = true;
    this.efrisService.generateInvoice(this.prepareSubmitData())
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


  queryTaxPayerInformation() {
    this.submitted = true;
    let request = new TaxPayer();
    request.tin = this.f.buyerTin.value;
    request.ninBrn = this.f.buyerNinBrn.value;
    this.loading[2] = true;
    this.efrisService.queryTaxPayerInformation(request)
      .pipe(first())
      .subscribe(
        response => {
          this.loading[2] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.submitted = false;
          let taxPayer = new TaxPayer();
          taxPayer = response.data;
          this.f.buyerTin.setValue(taxPayer.tin);
          this.f.buyerNinBrn.setValue(taxPayer.ninBrn);
          this.f.buyerLegalName.setValue(taxPayer.legalName);
          this.f.buyerBusinessName.setValue(taxPayer.businessName);
          this.f.buyerAddress.setValue(taxPayer.address);
          this.f.buyerEmail.setValue(taxPayer.contactEmail);
          this.f.buyerMobilePhone.setValue(taxPayer.contactNumber);
          this.f.buyerLinePhone.setValue(taxPayer.contactNumber);
          this.f.buyerPlaceOfBusi.setValue(taxPayer.address);
        },
        error => {
          this.loading[2] = false;
          this.submitted = false;
          this.alertService.displayError(error);
        });
  }

  async openGoodsAndServicesWindow() {
    this.ref = this.dialogService.open(ModalEfrisGoodsAndServiceComponent, {
      header: 'Goods and services',
      width: '40%',
      closable: true,
      styleClass: "p-dialog-titlebar",
      contentStyle: { "max-height": "500px", "overflow": "auto" },
      ////baseZIndex: 10000,,
      data: this.selectedData,
    });

    this.ref.onClose.subscribe((product: GoodsData) => {
      if (product) {
        this.openGoodsAndServicesMaintainWindow(product);
      }
    });
  }


  async openGoodsAndServicesMaintainWindow(product: GoodsData) {
    this.ref = this.dialogService.open(SupplierGoodsAndServicesComponent, {
      header: 'Goods and services',
      width: '40%',
      closable: true,
      styleClass: "p-dialog-titlebar",
      contentStyle: { "max-height": "500px", "overflow": "auto" },
      ////baseZIndex: 10000,,
      data: product,
    });

    this.ref.onClose.subscribe((product: GoodsDetail) => {
      if (product) {
        this.goodDetails.push(product);
        let summary = new Summary();
        let taxItem = new TaxDetail();
        let netAmount: number =  +product.total - +product.tax;
        summary.grossAmount = product.total;
        summary.netAmount = netAmount;
        summary.taxAmount = product.tax;
        
        taxItem.netAmount = netAmount;
        taxItem.taxRate = product.taxRate;
        taxItem.taxAmount = product.tax;
        taxItem.grossAmount = product.total;
        taxItem.exciseCurrency = "UGX";
        taxItem.taxCategoryCode = "01";
        this.taxDetails.push(taxItem);

        this.updateInvoiceSummary(summary);
      } else {
        // this.chartOfAcctData = null;
        // this.newGLChart = null;
        // this.chartOfAcctData = this.storedChartOfAcctData;
      }
    });
  }

  updateInvoiceSummary(summary: Summary) {
    if (summary == null) {
      this.invoiceSummary.netAmount = 0;
      this.invoiceSummary.taxAmount = 0;
      this.invoiceSummary.grossAmount = 0;
      this.invoiceSummary.itemCount = 0;
    } else {
      summary.grossAmount = summary.grossAmount == null ? 0 : summary.grossAmount;
      summary.netAmount = summary.netAmount == null ? 0 : summary.netAmount;
      summary.taxAmount = summary.taxAmount == null ? 0 : summary.taxAmount;

      this.invoiceSummary.netAmount = +this.invoiceSummary.netAmount + +summary.netAmount;
      this.invoiceSummary.taxAmount = +this.invoiceSummary.taxAmount + +summary.taxAmount;
      this.invoiceSummary.grossAmount = +this.invoiceSummary.grossAmount + +summary.grossAmount;
      this.invoiceSummary.itemCount = + 1;
      this.invoiceSummary.modeCode = "0";     
    }
  }
  

  onRemarksChanged() {
    this.invoiceSummary.remarks = this.f.remarks.value;
  }



  prepareSubmitData(): InvoiceData {
    let request = new InvoiceData;
    let buyerDetails = new BuyerDetails;
    buyerDetails.buyerTin = this.f.buyerTin.value;
    buyerDetails.buyerNinBrn = this.f.buyerNinBrn.value;
    buyerDetails.buyerPassportNum = this.f.buyerPassportNum.value;
    buyerDetails.buyerLegalName = this.f.buyerLegalName.value;
    buyerDetails.buyerBusinessName = this.f.buyerBusinessName.value;
    buyerDetails.buyerAddress = this.f.buyerAddress.value;
    buyerDetails.buyerEmail = this.f.buyerEmail.value;
    buyerDetails.buyerMobilePhone = this.f.buyerMobilePhone.value;
    buyerDetails.buyerLinePhone = this.f.buyerLinePhone.value;
    buyerDetails.buyerPlaceOfBusi = this.f.buyerPlaceOfBusi.value;
    buyerDetails.buyerType = this.f.buyerType.value;
    buyerDetails.buyerCitizenship = this.f.buyerCitizenship.value;
    buyerDetails.buyerSector = this.f.buyerSector.value;
    buyerDetails.buyerReferenceNo = this.f.buyerReferenceNo.value;
    request.buyerDetails = buyerDetails;
    request.taxDetails = this.taxDetails;
    request.goodsDetails = this.goodDetails;
    request.summary = this.invoiceSummary;
    request.createdBy = this.securityService.currentUser.userName;
    return request;
  }

  onClose() {
    this.navigation.goBack();
  }

}