package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class VoucherRequestData extends OutletAuthRequest {
    private BigDecimal transAmt;
    private String currency;
    private String sourceAcct;
    private String description;
    private String sourcePhoneNo;
    private String recipientPhone;
    private String voucherNo; // Optional for Voucher Buy.
    private String outletAcctNo;
}
