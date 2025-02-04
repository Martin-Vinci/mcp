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
public class CashOutResponse {
    private String accountNo;
    private String otpCode;
    private String phoneNo;
    private String customerName;
    private BigDecimal amount;
    private String expiryDate;
    private String codeStatus;
}
