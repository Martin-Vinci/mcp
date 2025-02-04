package com.greybox.mediums.models;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityAmount {
    private BigDecimal agentCommissionAmount;
    private BigDecimal withholdTax;
    private BigDecimal exciseDuty;
    private BigDecimal mobileMoneyTax;
    private BigDecimal maintenanceAmountShare;
    private BigDecimal totalCharge;
    private BigDecimal netCharge;
    private BigDecimal vendorShare;
    private BigDecimal bankShare;
    private BigDecimal vendorCommission;
}
