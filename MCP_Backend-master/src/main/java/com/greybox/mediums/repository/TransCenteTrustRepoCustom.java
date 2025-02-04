package com.greybox.mediums.repository;

import com.greybox.mediums.entities.TransCenteTrustSummary;
import com.greybox.mediums.models.CenteTrustAmountType;
import java.util.List;

public interface TransCenteTrustRepoCustom {
    List<TransCenteTrustSummary> findPendingCenteTrustTransactions();

    List<CenteTrustAmountType> findPostingAmountTypes(Integer paramInteger);

    void updatePostedTrustTransaction(String paramString1, String paramString2, Long paramLong, String paramString3, String paramString4);

    void deleteCenteTrustTransactionByTransId(Long paramLong);

    Long getTransactionRef();
}