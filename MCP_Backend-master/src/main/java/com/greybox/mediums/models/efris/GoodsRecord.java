package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoodsRecord {
    private String operationType;
    private String goodsName;
    private String goodsCode;
    private String measureUnit;
    private String unitPrice;
    private String currency;
    private String commodityCategoryId;
    private String haveExciseTax;
    private String description;
    private String stockPrewarning;
    private String pieceMeasureUnit;
    private String havePieceUnit;
    private String pieceUnitPrice;
    private String packageScaledValue;
    private String pieceScaledValue;
    private String exciseDutyCode;
    private String haveOtherUnit;
    private String returnMessage;
    private String returnCode;
    public String commodityCategoryName;
    public Integer pageNo;
    public Integer pageSize;
    public String branchId;
    private ArrayList<GoodsOtherUnit> goodsOtherUnits;
    private String id;
    private String stock;
    private String source;
    private String statusCode;
    private String commodityCategoryCode;
    private String taxRate;
    private String taxCategory;
    private String taxCategoryDescr;
    private String isZeroRate;
    private String isExempt;
    private String exciseDutyName;
    private String exciseRate;
    private String pack;
    private String stick;
    private String remarks;
    private String exclusion;






}