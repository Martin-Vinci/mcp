import { SelectionModel } from "@angular/cdk/collections";
import { Component, OnInit } from "@angular/core";
import { FormGroup, FormBuilder } from "@angular/forms";
import { ChargeTier } from "app/shared/models/charge-tier-data";
import { DictionaryData } from "app/shared/models/efris-dictionary";
import { GoodsData } from "app/shared/models/goods-data";
import { TaxPayer } from "app/shared/models/tax-payer";
import { EfrisService } from "app/shared/services/efris.service";
import * as moment from "moment";
import { MessageService } from "primeng/api";
import { DialogService, DynamicDialogRef, DynamicDialogConfig } from "primeng/dynamicdialog";
import { first } from "rxjs/operators";


@Component({
  selector: 'app-supplier-goods-and-services',
  templateUrl: './supplier-goods-and-services.component.html',
  styleUrls: ['./supplier-goods-and-services.component.scss']
})
export class SupplierGoodsAndServicesComponent implements OnInit {
  isTableLoading = false;
  submitted = false;
  dictionaryData = new DictionaryData();
  form: FormGroup;
  loading = false;
  errorMessage: string;
  selectedGood = new GoodsData();
  selection = new SelectionModel<ChargeTier>(false, []);
  public value: moment.Moment;
  constructor(
    private formBuilder: FormBuilder,
    public dialogService: DialogService,
    public messageService: MessageService,
    public ref: DynamicDialogRef,
    public config: DynamicDialogConfig,
    private efrisService: EfrisService
  ) {

  }

  ngOnInit() {
    this.selectedGood = this.config.data;
    this.querySystemDictionaryUpdate();
    this.setForm();
  }

  private setForm() {
    this.form = this.formBuilder.group({
      item: this.selectedGood.goodsName,
      itemCode: this.selectedGood.goodsCode,
      qty: 1,
      unitOfMeasure: this.selectedGood.measureUnit,
      unitPrice: null,
      total: this.selectedGood.unitPrice,
      taxRate: this.selectedGood.taxRate,
      tax: null,
      discountTotal: null,
      discountTaxRate: null,
      orderNumber: 0, // To be picked from the api
      discountFlag: 2,
      deemedFlag: 2,
      exciseFlag: 2,
      categoryId: null,
      categoryName: this.selectedGood.commodityCategoryName,
      goodsCategoryId: this.selectedGood.commodityCategoryCode,
      goodsCategoryName: null,
      exciseRate: null,
      exciseRule: null,
      exciseTax: null,
      pack: null,
      stick: null,
      exciseUnit: null,
      exciseCurrency: null,
      exciseRateName: null,
    });
  }

  get f() {
    return this.form.controls;
  }

  toFixedIfNecessary(value, dp) {
    return +parseFloat(value).toFixed(dp);
  }

  onUnitPriceChanged() {
    let unitPrice: number = this.f.unitPrice.value;
    let qty: number = this.f.qty.value;
    let total = unitPrice * qty;
    this.f.total.setValue(total);

    // Compute the tax
    //console.log("this.selectedGood.taxRate  " + this.selectedGood.taxRate);
    let grossVatRate: number = 1 + parseFloat(this.selectedGood.taxRate.toString());
    //console.log("grossVatRategrossVatRategrossVatRate " + grossVatRate);

    let taxAmount: number = +total - (+total / +grossVatRate);
    //console.log("======================================" + taxAmount);
    taxAmount = this.toFixedIfNecessary(taxAmount, 2)
    //console.log("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" + taxAmount);
    this.f.tax.setValue(taxAmount);
  }

  querySystemDictionaryUpdate() {
    this.submitted = true;
    let request = new TaxPayer();
    this.efrisService.querySystemDictionaryUpdate(request)
      .pipe(first())
      .subscribe(
        response => {
          this.dictionaryData = response.data;
        },
        error => {
          this.submitted = false;
        });
  }



  onSubmit() {
    this.errorMessage = "";
    if (this.form.invalid) {
      this.errorMessage = "Please fill all the fields";
      return;
    }

    this.ref.close(this.form.value);
  }
  cancel() {
    this.ref.close(false);
  }
}
