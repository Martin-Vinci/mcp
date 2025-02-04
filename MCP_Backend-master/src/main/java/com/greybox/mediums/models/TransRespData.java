package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransRespData {
    private Long transId;
    private BigDecimal chargeAmt;
    private BigDecimal drAcctBalance;
    private BigDecimal crAcctBalance;
    private String cbsTransId;
    private boolean isReversed;
    List<TransDetails> transDetails;
}