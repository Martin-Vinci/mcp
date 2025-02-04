package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasicInformation {
    private String invoiceNo;
    private String antifakeCode;
    private String deviceNo;
    private String issuedDate;
    private String operator;
    private String currency;
    private String oriInvoiceId;
    private String invoiceType;
    private String invoiceKind;
    private String dataSource;
    private String invoiceIndustryCode;
    private String isBatch;
}
