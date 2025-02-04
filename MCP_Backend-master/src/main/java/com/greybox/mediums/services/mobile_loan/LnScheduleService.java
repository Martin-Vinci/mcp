package com.greybox.mediums.services.mobile_loan;


import com.greybox.mediums.entities.mobile_loan.LnSchedule;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.mobile_loan.LoanScheduleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LnScheduleService {

    @Autowired
    private LoanScheduleRepo loanScheduleRepo;

    public TxnResult findAll(LnSchedule request) {
        List<LnSchedule> schedules = loanScheduleRepo.findAll(request.getLoanId());
        if (schedules == null || schedules.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        List<LnSchedule> lnScheduleList = new ArrayList<>();
        for (LnSchedule item : schedules){
            item.setTotalAmount(item.getPrincipalAmount() + item.getInterestAmount());
            item.setAmountUnPaid(item.getInterestUnpaid() + item.getPrincipalUnpaid());
            item.setAmountPaid(item.getInterestPaid() + item.getPrincipalPaid());
            lnScheduleList.add(item);
        }
        return TxnResult.builder().message("approved").
                code("00").data(lnScheduleList).build();
    }


    public TxnResult save(LnSchedule request) {
        loanScheduleRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(LnSchedule request) {
        loanScheduleRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
