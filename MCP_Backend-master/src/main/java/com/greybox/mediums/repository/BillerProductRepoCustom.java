package com.greybox.mediums.repository;


import com.greybox.mediums.entities.BillerProduct;
import com.greybox.mediums.models.ISWBillerItem;

import java.math.BigDecimal;
import java.util.List;

public interface BillerProductRepoCustom {
    List<BillerProduct> findBillerProductsByBillerId(BillerProduct paramBillerProduct);
    public List<BillerProduct> findMobileBillerProductsByBiller(BillerProduct request);
    void updateBillerProductPrice(String paramString1, Integer paramInteger, BigDecimal paramBigDecimal, String paramString2);

    void updateBillerProductPrice(Integer paramInteger);

    ISWBillerItem findISWPaymentItem(String paramString1, String paramString2);
}

