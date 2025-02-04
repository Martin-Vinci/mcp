package com.greybox.mediums.services.mobile_loan;

import com.greybox.mediums.entities.mobile_loan.LnAccount;
import com.greybox.mediums.entities.mobile_loan.LnSchedule;
import com.greybox.mediums.entities.mobile_loan.LoanRepaymentHistory;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.mobile_loan.LoanAccountRepo;
import com.greybox.mediums.repository.mobile_loan.LoanRepaymentRepo;
import com.greybox.mediums.repository.mobile_loan.LoanScheduleRepo;
import com.greybox.mediums.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Service
public class RepaymentService {

    @Autowired
    private LoanRepaymentRepo loanRepaymentRepo;
    @Autowired
    private LoanScheduleRepo loanScheduleRepo;
    @Autowired
    private LoanAccountRepo loanAccountRepo;

    public TxnResult findAll(LoanRepaymentHistory request) {
        List<LoanRepaymentHistory> charges = loanRepaymentRepo.findAll();
        if (charges == null || charges.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(charges).build();
    }


    @Transactional
    public TxnResult save(LoanRepaymentHistory request) {
        LnAccount lnAccount = loanAccountRepo.findLoanAccount(request.getAcctNo());
        if (lnAccount.getLoanId() == null)
            return TxnResult.builder().message("Invalid Loan account specified").
                    code("404").data(request).build();

        request.setTxnRef(String.valueOf(System.currentTimeMillis()));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        request.setTransDate(timestamp);
        request.setSuccessFlag("Y");
        LoanRepaymentHistory response = loanRepaymentRepo.save(request);

        // Get Loan account schedules
        Double paymentAmount = request.getTransAmount().doubleValue();
        Double totalPayment = 0D;
        Double totalPrincipal = 0D;
        Double totalRepayment = 0D;
        List<LnSchedule> scheduleList = loanScheduleRepo.findPendingSchedules(lnAccount.getLoanId());

        for (LnSchedule schedule : scheduleList) {
            totalRepayment += schedule.getInterestUnpaid() + schedule.getPrincipalUnpaid();
        }
        if (totalRepayment < paymentAmount) {
            return TxnResult.builder().message("Specified Payment amount [" + String.format("%.2f", paymentAmount) + "] is greater than the expected repayment amount [" + String.format("%.2f", totalRepayment) + "].").code("99").data(request).build();
        }

        for (LnSchedule schedule : scheduleList) {
            totalPayment = schedule.getInterestUnpaid() + schedule.getPrincipalUnpaid();
            if (totalPayment <= paymentAmount) {
                schedule.setInterestPaid(schedule.getInterestAmount());
                schedule.setInterestUnpaid(0d);
                schedule.setPrincipalPaid(schedule.getPrincipalAmount());
                schedule.setPrincipalUnpaid(0d);
                schedule.setStatus("PAID");
                schedule.setPaymentDate(DataUtils.getCurrentDate().toLocalDate());
                loanScheduleRepo.save(schedule);
                totalPrincipal += schedule.getPrincipalAmount();
                paymentAmount = paymentAmount - totalPayment;
                continue;
            }

            if (paymentAmount > schedule.getInterestUnpaid()) {
                paymentAmount = paymentAmount - schedule.getInterestUnpaid();
                schedule.setInterestPaid(schedule.getInterestUnpaid());
                schedule.setInterestUnpaid(0d);
            } else {
                schedule.setInterestPaid(paymentAmount);
                schedule.setInterestUnpaid(schedule.getInterestUnpaid() - paymentAmount);
                paymentAmount = 0D;
            }

            if (paymentAmount > 0) {
                totalPrincipal += paymentAmount;
                schedule.setPrincipalPaid(paymentAmount + schedule.getPrincipalPaid());
                schedule.setPrincipalUnpaid(schedule.getPrincipalUnpaid() - paymentAmount);
            }
            schedule.setStatus("PARTIALLY_PAID");
            schedule.setPaymentDate(DataUtils.getCurrentDate().toLocalDate());
            loanScheduleRepo.save(schedule);
            break;
        }

        // Get the Next Schedule to be Serviced in Future
        LnSchedule schedule = loanScheduleRepo.findMinimumUnPaidSchedule(lnAccount.getLoanId());
        if (schedule != null) {
            lnAccount.setNextPmtAmount(schedule.getInterestUnpaid() + schedule.getPrincipalUnpaid());
            lnAccount.setLedgerBal(lnAccount.getLedgerBal().subtract(new BigDecimal(totalPrincipal)));
            lnAccount.setNextPmtDate(schedule.getDueDate());
        } else {
            lnAccount.setNextPmtAmount(0D);
            lnAccount.setLedgerBal(BigDecimal.ZERO);
            lnAccount.setNextPmtDate(null);
            lnAccount.setStatus("CLOSED");
        }
        loanAccountRepo.save(lnAccount);
        return TxnResult.builder().message("approved").code("00").data(request).build();
    }
}
