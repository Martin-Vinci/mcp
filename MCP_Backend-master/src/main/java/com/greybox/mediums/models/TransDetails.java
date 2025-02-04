package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransDetails {
    private String destAcctNo;
    private BigDecimal transAmt;
    private String sourceAcctNo;
    private String description;
    private String amountType;
    private String transType;
    private String cbsTransId;
    private long mcpTransId;
    private long mcpTransDetailId;
    private LocalDate createDate;
    private String reversalFailureReason;
    private boolean isMain;
}
