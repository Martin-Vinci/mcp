package com.greybox.mediums.services;

import com.greybox.mediums.entities.BillerNotifLog;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.BillerNotifLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillerNotifLogService {

    @Autowired
    private BillerNotifLogRepo billerNotifLogRepo;
    public TxnResult find(BillerNotifLog request) {
        List<BillerNotifLog> customers = billerNotifLogRepo.findBillPaymentLog();
        if (customers == null)
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult save(BillerNotifLog request) {
        billerNotifLogRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(BillerNotifLog request) {
        billerNotifLogRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
