package com.greybox.mediums.models.equiweb;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CIStatementResponse {
    private String transDate;
    private String effectiveDate;
    private String reference;
    private String credit;
    private String debit;
    private BigDecimal txnAmount;
    private BigDecimal closing;
    private String description;
}
