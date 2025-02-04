package com.greybox.mediums.models.equiweb;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StatementOutput {
    private String description;
    private String transDate;
    private String transAmount;
    private String  closingBalance;
    private String debitCredit;
}
