package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterswitchPaymentItem {
    private Integer categoryid;
    private Integer billerid;
    private boolean isAmountFixed;
    private Integer paymentitemid;
    private String paymentitemname;
    private BigDecimal amount;
    private String code;
    private String currencyCode;
    private String currencySymbol;
    private String itemCurrencySymbol;
    private String sortOrder;
    private String pictureId;
    private String paymentCode;
}
