import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { EfrisCommodity, EfrisCommodityCode } from 'app/shared/models/efris-commodity';
import { DictionaryData } from 'app/shared/models/efris-dictionary';
import { BuyerDetails, GoodsDetail, InvoiceData, Summary } from 'app/shared/models/efris-invoice-data';
import { GoodsData } from 'app/shared/models/goods-data';
import { TaxPayer } from 'app/shared/models/tax-payer';
import { AlertService } from 'app/shared/services/alert.service';
import { EfrisService } from 'app/shared/services/efris.service';
import { RouterServiceService } from 'app/shared/services/router-service.service';
import { StorageService } from 'app/shared/services/storage.service';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { first } from 'rxjs/operators';
import { EfrisCommoditiesComponent } from '../efris-commodities/efris-commodities.component';


@Component({
  selector: 'app-goods-and-services-maintain',
  templateUrl: './goods-and-services-maintain.component.html',
  styleUrls: ['./goods-and-services-maintain.component.scss']
})
export class GoodsAndServicesMaintainComponent implements OnInit {
  loading = [false, false, false];
  submitted = false;
  dictionaryData = new DictionaryData();
  isEdit = false;
  isView = false;
  isSuperAgent = false;
  canAddAcct = false;
  isAddMode = false;
  cssBodyWidth: string = "col-10";
  form: FormGroup;
  action: string;
  goodDetails = [];
  selectedGood: GoodsData;
  ref: DynamicDialogRef;
  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private navigation: RouterServiceService,
    private storageService: StorageService,
    private alertService: AlertService,
    public dialogService: DialogService,
    private efrisService: EfrisService
  ) {

  }

  ngOnInit() {
    this.action = this.route.snapshot.params['id'];
    this.createForms();
    this.querySystemDictionaryUpdate(); 
    this.prepareFormBasedOnAction();
    if (this.isAddMode == false) {
      this.findEntityData();
    }
  }

  ngOnDestroy() {
    ///sessionStorage.removeItem('GoodsDetails');
  }


  prepareFormBasedOnAction() {
    if (this.action == "Add") {
      this.isAddMode = !this.selectedGood;
    }
    else if (this.action == "View") {
      this.isView = true;
      this.selectedGood = this.storageService.getSelectedProduct();
      this.isAddMode = !this.selectedGood;
      //this.getAccounts();
    }
  }

  createForms() {
    this.form = this.fb.group({
      operationType: '101',
      goodsName:null,
      goodsCode: null,
      measureUnit:null,
      unitPrice: null,
      currency: null,
      commodityCategoryId: null,
      haveExciseTax: '102',
      description: '1',
      stockPrewarning: null,
      pieceMeasureUnit: null,
      havePieceUnit: '102',
      pieceUnitPrice: null,
      packageScaledValue: null,
      pieceScaledValue: null,
      exciseDutyCode:  null,
      haveOtherUnit: '102',
      edit: false
    });
  }

  get f() {
    return this.form.controls;
  }

  findEntityData() {
    try {
      this.f.operationType.setValue(this.selectedGood.operationType);
      this.f.goodsName.setValue(this.selectedGood.goodsName);
      this.f.goodsCode.setValue(this.selectedGood.goodsCode);
      this.f.measureUnit.setValue(this.selectedGood.measureUnit);
      this.f.unitPrice.setValue(this.selectedGood.unitPrice);
      this.f.currency.setValue(this.selectedGood.currency);
      this.f.commodityCategoryId.setValue(this.selectedGood.commodityCategoryCode + '-'+this.selectedGood.commodityCategoryName);
      this.f.haveExciseTax.setValue(this.selectedGood.haveExciseTax);
      this.f.description.setValue(this.selectedGood.description);
      this.f.stockPrewarning.setValue(this.selectedGood.stockPrewarning);
      this.f.pieceMeasureUnit.setValue(this.selectedGood.pieceMeasureUnit);
      this.f.havePieceUnit.setValue(this.selectedGood.havePieceUnit);
      this.f.pieceUnitPrice.setValue(this.selectedGood.pieceUnitPrice);
      this.f.packageScaledValue.setValue(this.selectedGood.packageScaledValue);
      this.f.pieceScaledValue.setValue(this.selectedGood.pieceScaledValue);
      this.f.exciseDutyCode.setValue(this.selectedGood.exciseDutyCode);
      this.f.haveOtherUnit.setValue(this.selectedGood.haveOtherUnit);
      //this.f.goodsOtherUnits: ArrayList<GoodsOtherUnit>;
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
    this.efrisService.goodsUpload(this.prepareSubmitData())
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


  querySystemDictionaryUpdate() {
    this.submitted = true;
    let request = new TaxPayer();
    this.loading[2] = true;
    this.efrisService.querySystemDictionaryUpdate(request)
      .pipe(first())
      .subscribe(
        response => {
          console.log(response);
          this.loading[2] = false;
          if (response.code !== "00") {
            this.alertService.displayError(response.message);
            return;
          }
          this.dictionaryData = response.data;
        },
        error => {
          this.loading[2] = false;
          this.submitted = false;
          this.alertService.displayError(error);
        });
  }

  async openCommoditiesCategories() {
    this.ref = this.dialogService.open(EfrisCommoditiesComponent, {
      header: 'Commodities',
      width: '40%',
      closable: true,
      baseZIndex: 10000,
    });

    this.ref.onClose.subscribe((product: EfrisCommodityCode) => {
      if (product) {
        this.f.commodityCategoryId.setValue(product.commodityCategoryLevel4Code);
      }
    });
  }

  prepareSubmitData(): GoodsData {
    let request = new GoodsData;
    request.operationType = this.f.operationType.value;
    request.goodsName = this.f.goodsName.value;
    request.goodsCode = this.f.goodsCode.value;
    request.measureUnit = this.f.measureUnit.value;
    request.unitPrice = this.f.unitPrice.value;
    request.currency = this.f.currency.value;
    var codeDetail = this.f.commodityCategoryId.value.split("-"); 
    request.commodityCategoryId = codeDetail[0];
    request.haveExciseTax = this.f.haveExciseTax.value;
    request.description = this.f.description.value;
    request.stockPrewarning = this.f.stockPrewarning.value;
    request.pieceMeasureUnit = this.f.pieceMeasureUnit.value;
    request.havePieceUnit = this.f.havePieceUnit.value;
    request.pieceUnitPrice = this.f.pieceUnitPrice.value;
    request.packageScaledValue = this.f.packageScaledValue.value;
    request.pieceScaledValue = this.f.pieceScaledValue.value;
    request.exciseDutyCode = this.f.exciseDutyCode.value;
    request.haveOtherUnit = this.f.haveOtherUnit.value;
    return request;
  }

  onClose() {
    this.navigation.goBack();
  }




}