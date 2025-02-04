package com.greybox.mediums.models.equiweb;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CIAccountBalance {
    private String accountTitle;
    private String branchName;
    private BigDecimal availableBalance;
    private String currency;
    private String accountStatus;
    private String customerNo;
}
