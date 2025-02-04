package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoucherResponse {
    private String voucherNo;
    private String transDate;
    private String expiryDate;
    private String status;
    private String sourcePhoneNo;
    private String recipientPhoneNo;
    private BigDecimal transAmount;
    private BigDecimal chargeAmount;
    private String description;
    private String senderName;
    private BigDecimal sourceAcctBal;

}
