package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummary {
    private String serviceCode;
    private String description;
    private String status;
    private Integer transactionCount;
    private BigDecimal transactionVolume;
    private BigDecimal agentCommission;
    private BigDecimal totalCharge;
}
