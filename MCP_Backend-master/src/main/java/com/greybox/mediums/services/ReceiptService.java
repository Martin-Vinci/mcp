package com.greybox.mediums.services;

import com.greybox.mediums.entities.IssuedReceipt;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.IssuedReceiptRepo;
import com.greybox.mediums.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReceiptService {

    @Autowired
    private IssuedReceiptRepo issuedReceiptRepo;

    public TxnResult find(IssuedReceipt request) {
        request.setFromDate(request.getFromDate() == null ? DataUtils.getCurrentDate().toLocalDate() : request.getFromDate());
        request.setToDate(request.getToDate() == null ? DataUtils.getCurrentDate().toLocalDate() : request.getToDate());
        List<IssuedReceipt> customers;
        if (request.getTxnId() != null)
            customers = issuedReceiptRepo.findIssuedReceipts(request.getOutletCode(), request.getTxnId());
        else
            customers = issuedReceiptRepo.findIssuedReceipts(request.getOutletCode(), request.getFromDate(), request.getToDate());

        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult save(IssuedReceipt request) {
        issuedReceiptRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
