package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AirlineGoodsDetails {
    private String item;
    private String itemCode;
    private String qty;
    private String unitOfMeasure;
    private String unitPrice;
    private String total;
    private String taxRate;
    private String tax;
    private String discountTotal;
    private String discountTaxRate;
    private String orderNumber;
    private String discountFlag;
    private String deemedFlag;
    private String exciseFlag;
    private String categoryId;
    private String categoryName;
    private String goodsCategoryId;
    private String goodsCategoryName;
    private String exciseRate;
    private String exciseRule;
    private String exciseTax;
    private String pack;
    private String stick;
    private String exciseUnit;
    private String exciseCurrency;
    private String exciseRateName;
}
