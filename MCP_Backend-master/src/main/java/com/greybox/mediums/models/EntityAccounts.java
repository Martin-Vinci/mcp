package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityAccounts {
    private String bankCommission;
    private String collectionAccount;
    private String micropayCommission;
    private String maintenanceAccount;
    private String expenseAccount;
    private String agentCommission;
    private String transitAccount;
    private String withholdTaxAccount;
    private String exciseTaxAccount;
    private String mobileMoneyTaxAccount;
    private String suspenseAccount;
    private String centeAgentChargeExpenseAccount;
    private String bankOperationAccount;
    private String bankTrustAccount;
    private String vendorCommissionAccount;
    private String airtelCommissionReceivable;
    private String interSwitchCommissionReceivable;
    private String lycaMobileCommissionReceivable;
}
