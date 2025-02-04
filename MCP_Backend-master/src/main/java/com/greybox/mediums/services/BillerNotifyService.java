package com.greybox.mediums.services;

import com.greybox.mediums.entities.BillerNotif;
import com.greybox.mediums.models.ErrorData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.BillerNotifRepo;
import com.greybox.mediums.repository.ReportRepo;
import com.greybox.mediums.utils.MediumException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillerNotifyService {

    @Autowired
    private BillerNotifRepo billerNotifRepo;
    @Autowired
    private ReportRepo reportRepo;

    public TxnResult find(BillerNotif request) {
        if (request.getBillerCode() != null)
            if (request.getBillerCode().equals(""))
                request.setBillerCode(null);

        if (request.getChannelCode() != null)
            if (request.getChannelCode().equals(""))
                request.setChannelCode(null);

        List<BillerNotif> customers = reportRepo.findBillerNotifications(request);
        if (customers == null)
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult findBillerNotificationByReferenceNo(BillerNotif request) throws MediumException {
        if (request.getChannelCode() == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid channel code specified").build());
        if (request.getBillerCode() == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid biller code specified").build());
        if (request.getThirdPartyReference() == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid third party reference specified").build());

        BillerNotif customers = billerNotifRepo.findBillerNotificationByReferenceNo(
                request.getBillerCode(),
                request.getChannelCode(),
                request.getThirdPartyReference()
        );
        if (customers == null)
            return TxnResult.builder().code("404")
                    .message("System could not locate the third party reference at Micropay Core")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }


    public TxnResult save(BillerNotif request) {
        BillerNotif billerNotif = billerNotifRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(billerNotif).build();
    }

    public TxnResult updateBillStatus(BillerNotif request) {
        //request.setReversalReason(request.getReversalFlag() == null ? "" : request.getReversalFlag());
        //request.setExtenalTransRef(request.getExtenalTransRef() == null ? "" : request.getExtenalTransRef());
        // billerNotifRepo.updateBillNotifiation(request.getId(), request.getStatus(), request.getReversalReason(), request.getExtenalTransRef());
        billerNotifRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(BillerNotif request) {
        billerNotifRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
