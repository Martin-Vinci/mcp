package com.greybox.mediums.models;

import com.greybox.mediums.inter_switch.dto.PaymentRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class TransRequestData extends OutletAuthRequest {
    private String destAcctNo;
    private BigDecimal transAmt;
    private BigDecimal additionalCharge;
    private String currency;
    private String sourceAcctNo;
    private String description;
    private Integer serviceCode;
    private String externalReference;
    private Long originTransId;
    private String depositorPhoneNo;
    private String depositorName;
    private PaymentRequest paymentRequest;
}
