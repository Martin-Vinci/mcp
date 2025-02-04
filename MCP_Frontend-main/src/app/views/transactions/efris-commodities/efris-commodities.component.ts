import { Component, OnInit } from "@angular/core";
import { FormGroup, FormBuilder } from "@angular/forms";
import { DatePipeService } from "app/shared/helpers/date-pipe.service";
import { EfrisCommodity } from "app/shared/models/efris-commodity";
import { GoodsDetail } from "app/shared/models/efris-invoice-data";
import { AlertService } from "app/shared/services/alert.service";
import { EfrisService } from "app/shared/services/efris.service";
import * as moment from "moment";
import { MessageService } from "primeng/api";
import { DialogService, DynamicDialogRef, DynamicDialogConfig } from "primeng/dynamicdialog";
import { first } from "rxjs/operators";

@Component({
  selector: 'app-efris-commodities',
  templateUrl: './efris-commodities.component.html',
  styleUrls: ['./efris-commodities.component.scss']
})
export class EfrisCommoditiesComponent implements OnInit {
  isTableLoading = false;
  form: FormGroup;
  loading = [false, false, false, false, false, false]
  submitted = false;
  errorMessage: string;
  selectedUser = new GoodsDetail();
  level1CommodityList = [];
  level2CommodityList = [];
  level3CommodityList = [];
  level4CommodityList = [];
  public value: moment.Moment;
  constructor(
    private formBuilder: FormBuilder,
    public dialogService: DialogService,
    public messageService: MessageService,
    public ref: DynamicDialogRef,
    public config: DynamicDialogConfig,
    private alertService: AlertService,
    private efrisService: EfrisService,
    public datepipe: DatePipeService,

  ) {

  }

  ngOnInit() {
    //this.selectedUser = this.config.data;
    this.setForm();
    this.queryCommodityCategoryByParentCode("0", 1);
  }

  private setForm() {
    this.form = this.formBuilder.group({
      commodityCategoryLevel1Code: null,
      commodityCategoryLevel2Code: null,
      commodityCategoryLevel3Code: null,
      commodityCategoryLevel4Code: null,
      commodityCategoryLevel5Code: null,
    });
  }

  get f() {
    return this.form.controls;
  }


  queryCommodityCategoryByParentCode(parentCode: string, levelNumber: number) {
    this.submitted = true;
    let request = new EfrisCommodity();
    var codeDetail = parentCode.split("-"); 
    request.parentCode = codeDetail[0];
    this.enableLoader(levelNumber);
    this.errorMessage = "";  
    this.efrisService.queryCommodityCategoryByParentCode(request)
      .pipe(first())
      .subscribe(
        response => {
          this.disableLoader(levelNumber);
          if (response.code !== "00") {
            this.errorMessage = response.message;
            return;
          }
          if (levelNumber == 1) {
            this.level1CommodityList = response.data;
          }
          if (levelNumber == 2)
            this.level2CommodityList = response.data;
          if (levelNumber == 3)
            this.level3CommodityList = response.data;
          if (levelNumber == 4)
            this.level4CommodityList = response.data;
        },
        error => {
          this.loading[2] = false;
          this.submitted = false;
          this.alertService.displayError(error);
        });
  }


  enableLoader(levelNumber: number) {
    if (levelNumber == 1)
      this.loading[1] = true;
    if (levelNumber == 2)
    this.loading[2] = true;
    if (levelNumber == 3)
    this.loading[3] = true;
    if (levelNumber == 4)
    this.loading[4] = true;
  }

  disableLoader(levelNumber: number) {
    if (levelNumber == 1)
      this.loading[1] = false;
    if (levelNumber == 2)
    this.loading[2] = false;
    if (levelNumber == 3)
    this.loading[3] = false;
    if (levelNumber == 4)
    this.loading[4] = false;
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
