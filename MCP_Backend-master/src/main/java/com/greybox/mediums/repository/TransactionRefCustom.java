package com.greybox.mediums.repository;

import com.greybox.mediums.entities.TransCenteTrustSummary;
import com.greybox.mediums.models.CenteTrustAmountType;

import java.util.List;

public interface TransactionRefCustom {
    public void updateTxnDetailStatus(long txnId, String reversalFlag, String remarks,
                                      String status, String cbsTxnId);
    void updateTransactionStatus(Long txnId, String successFlag, String utilPosted,
                                 String reversalFlag, String reason, String txnMode);

    void updateAllPendingTxnStatuses(long txnId, String reversalFlag, String remarks, String status);

}

