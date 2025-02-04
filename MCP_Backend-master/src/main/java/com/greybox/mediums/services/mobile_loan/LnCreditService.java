package com.greybox.mediums.services.mobile_loan;

import com.greybox.mediums.entities.mobile_loan.LnCreditApp;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.mobile_loan.CreditAppRepo;
import com.greybox.mediums.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LnCreditService {

    @Autowired
    private CreditAppRepo creditAppRepo;

    public TxnResult findAll(LnCreditApp request) {
        List<LnCreditApp> charges;
        if (request.getCustId() != null)
            charges = creditAppRepo.findAll(request.getCustId());
        else
            charges = creditAppRepo.findCreditAppl(request);
        if (charges == null || charges.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(charges).build();
    }

    public TxnResult save(LnCreditApp request) {
        String repaymentPeriod;
        if (request.getRepayPeriod() == null)
            return TxnResult.builder().message("Repayment period cannot be empty").
                    code("-99").build();

        switch (request.getRepayPeriod()) {
            case "D":
                repaymentPeriod = "DAY";
                break;
            case "W":
                repaymentPeriod = "WEEKS";
                break;
            default:
                repaymentPeriod = "MONTH";
        }
        request.setRepayPeriod(repaymentPeriod);
        request.setCreateDate(DataUtils.getCurrentDate().toLocalDate());
        request.setCreditType("MOBILE_LOAN");
        request.setStatus("PENDING");
        request.setRowVersion(1);
        creditAppRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(LnCreditApp request) {
        creditAppRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
