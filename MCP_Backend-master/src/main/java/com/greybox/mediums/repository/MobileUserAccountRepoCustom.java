package com.greybox.mediums.repository;


import com.greybox.mediums.entities.TransactionRef;

import java.math.BigDecimal;

public interface MobileUserAccountRepoCustom {
    void updateAccountBalance(String accountId, BigDecimal currentBalance);

}

