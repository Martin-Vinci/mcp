package com.greybox.mediums.services.mobile_loan;

import com.greybox.mediums.entities.mobile_loan.LnAccount;
import com.greybox.mediums.entities.mobile_loan.LnCreditApp;
import com.greybox.mediums.entities.mobile_loan.LnRepaymentInfo;
import com.greybox.mediums.entities.mobile_loan.LnSchedule;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.mobile_loan.CreditAppRepo;
import com.greybox.mediums.repository.mobile_loan.LoanScheduleRepo;
import com.greybox.mediums.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class LoanManagerService {

    @Autowired
    private LnAccountService lnAccountService;
    @Autowired
    private LnScheduleService lnScheduleService;
    @Autowired
    private LoanScheduleRepo loanScheduleRepo;
    @Autowired
    private CreditAppRepo creditAppRepo;

    Double totalInterest = 0D;
    Double totalPrincipal = 0D;
    Date maturityDate;

    Date getNextDate(Date payDueDate, String period) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(payDueDate);
        switch (period) {
            case "DAY":
                cal.add(Calendar.DATE, 1);
                payDueDate = cal.getTime();
                break;
            case "WEEK":
                cal.add(Calendar.DATE, 7);
                payDueDate = cal.getTime();
                break;
            case "MONTH":
                cal.add(Calendar.MONTH, 1);
                payDueDate = cal.getTime();
                break;
            case "Quarter":
                cal.add(Calendar.MONTH, 3);
                payDueDate = cal.getTime();
                break;
            case "Half-Year":
                cal.add(Calendar.MONTH, 6);
                payDueDate = cal.getTime();
                break;
            case "Year":
                cal.add(Calendar.MONTH, 12);
                payDueDate = cal.getTime();
                break;
        }
        return payDueDate;
    }

    Integer getFrequencyNumberEquivalent(String strInterval) {
        Integer intpnPeriods = null;
        switch (strInterval) {
            case "DAY":
                intpnPeriods = 365;
                break;
            case "WEEK":
                intpnPeriods = 52;
                break;
            case "MONTH":
                intpnPeriods = 12;
                break;
            case "Quarter":
                intpnPeriods = 4;
                break;
            case "Half-Year":
                intpnPeriods = 2;
                break;
            case "Year":
                intpnPeriods = 1;
                break;
            default:
                int i = 1;
        }
        return intpnPeriods;
    }


    private List<LnSchedule> computeFlatAmountSchedule(LnCreditApp request, LnAccount lnAccount, LnRepaymentInfo lnRepaymentInfo) {
        List<LnSchedule> lnSchedulesList = new ArrayList<>();
        int intCount;
        Double dblPrinciple;
        Double dblTempPrinciple = 0d;
        Double dblTotal;
        int intTotMonths;
        Double dblInterest;
        Double dblInterestRate;
        Double dblTotalAmount = 0D;
        String strInterval;
        int intpnPeriods = 0;
        Double dblnPayments;
        Double dblnPvRate = 0D;
        Date payDueDate = java.sql.Date.valueOf(request.getStartDate());
        intTotMonths = request.getRepayTerm();
        strInterval = request.getRepayPeriod();
        dblTotal = request.getApplAmt().doubleValue();
        dblPrinciple = (dblTotal / intTotMonths);
        // USING SIMPLE INTEREST
        dblInterest = (dblPrinciple
                * (lnRepaymentInfo.getInterestRate() / 100));
        for (intCount = 0; (intCount
                <= (intTotMonths - 1)); intCount++) {
            int serial = (intCount + 1);
            dblInterest = dblInterest;
            dblPrinciple = dblPrinciple;
            dblTempPrinciple = (dblTempPrinciple - dblPrinciple);
            dblTotalAmount = dblPrinciple + dblInterest;

            lnSchedulesList.add(new LnSchedule(
                    serial,
                    lnAccount.getLoanId(),
                    dblPrinciple,
                    dblInterest,
                    0D,
                    0D,
                    dblPrinciple,
                    dblInterest,
                    payDueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    null,
                    "NOT_PAID",
                    lnAccount.getCreatedBy(),
                    DataUtils.getCurrentDate().toLocalDate(),
                    0));

            maturityDate = payDueDate;
            Calendar cal = Calendar.getInstance();
            cal.setTime(payDueDate);
            switch (strInterval) {
                case "DAY":
                    cal.add(Calendar.DATE, 1);
                    payDueDate = cal.getTime();
                    break;
                case "WEEK":
                    cal.add(Calendar.DATE, 7);
                    payDueDate = cal.getTime();
                    break;
                case "MONTH":
                    cal.add(Calendar.MONTH, 1);
                    payDueDate = cal.getTime();
                    break;
                case "Quarter":
                    cal.add(Calendar.MONTH, 3);
                    payDueDate = cal.getTime();
                    break;
                case "Half-Year":
                    cal.add(Calendar.MONTH, 6);
                    payDueDate = cal.getTime();
                    break;
                case "Year":
                    cal.add(Calendar.MONTH, 12);
                    payDueDate = cal.getTime();
                    break;
            }
        }
        // Get the total Interest to be paid.
        totalInterest = 0D;
        totalPrincipal = 0D;
        for (LnSchedule lsr : lnSchedulesList) {
            totalInterest = totalInterest + lsr.getInterestAmount();
            totalPrincipal = totalPrincipal + lsr.getPrincipalAmount();
        }
        return lnSchedulesList;
    }

    private List<LnSchedule> computeReducingBalanceSchedule(LnCreditApp request, LnAccount lnAccount, LnRepaymentInfo lnRepaymentInfo) {
        List<LnSchedule> lnSchedulesList = new ArrayList<>();
        int intCount;
        Double dblPrinciple;
        Double dblTempPrinciple = 0d;
        Double totalLoanAmount;
        int intTotMonths;
        Double dblInterest;
        Double dblInterestRate;
        Double dblTotalAmount = 0D;
        String strInterval;
        int intpnPeriods = 0;
        Double dblnPayments;
        Double dblnPvRate = 0D;
        Date payDueDate = getNextDate(java.sql.Date.valueOf(request.getStartDate()), request.getRepayPeriod());
        intTotMonths = request.getRepayTerm();
        strInterval = request.getRepayPeriod();
        totalLoanAmount = request.getApplAmt().doubleValue();
        dblPrinciple = (totalLoanAmount / intTotMonths);
        if ((lnRepaymentInfo.getRepaymentType().equals("EQUAL_TOTAL_PAYMENT"))) // Using  equal total payments
        {
            dblInterestRate = (lnRepaymentInfo.getInterestRate() / 100);
            dblTempPrinciple = totalLoanAmount;
            intpnPeriods = getFrequencyNumberEquivalent(strInterval);
            intCount = 1;
            dblnPvRate = 0D;
            while ((intCount <= intTotMonths)) {
                dblnPvRate = (dblnPvRate + ((1 / (1 + (dblInterestRate / intpnPeriods)))));
                intCount = (intCount + 1);
            }
            dblnPayments = (dblTempPrinciple / dblnPvRate);
            for (intCount = 0; (intCount
                    <= (intTotMonths - 1)); intCount++) {
                int serial = (intCount + 1);
                // calculate interest part of Month(s) repayment
                dblInterest = (dblTempPrinciple * (dblInterestRate / intpnPeriods));
                // calculate principal part of Month(s) repayment
                dblPrinciple = (dblnPayments - dblInterest);
                dblTempPrinciple = (dblTempPrinciple - dblPrinciple);
                dblTotalAmount = (dblPrinciple + dblInterest);
                lnSchedulesList.add(new LnSchedule(
                        serial,
                        lnAccount.getLoanId(),
                        dblPrinciple,
                        dblInterest,
                        0D,
                        0D,
                        dblPrinciple,
                        dblInterest,
                        payDueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        null,
                        "NOT_PAID",
                        lnAccount.getCreatedBy(),
                        DataUtils.getCurrentDate().toLocalDate(),
                        0)
                );
                maturityDate = payDueDate;
                payDueDate = getNextDate(payDueDate, strInterval);
            }
        } else {
            for (intCount = 0; (intCount
                    <= (intTotMonths - 1)); intCount++) {
                dblInterest = (totalLoanAmount * ((lnRepaymentInfo.getInterestRate() / 100)));
                int serial = (intCount + 1);
                dblTempPrinciple = (dblTempPrinciple - dblPrinciple);
                dblTotalAmount = (dblPrinciple + dblInterest);
                lnSchedulesList.add(new LnSchedule(
                        serial,
                        lnAccount.getLoanId(),
                        dblPrinciple,
                        dblInterest,
                        0D,
                        0D,
                        dblPrinciple,
                        dblInterest,
                        payDueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        null,
                        "NOT_PAID",
                        lnAccount.getCreatedBy(),
                        DataUtils.getCurrentDate().toLocalDate(),
                        0));
                maturityDate = payDueDate;
                payDueDate = getNextDate(payDueDate, strInterval);
                totalLoanAmount = (totalLoanAmount - dblPrinciple);
            }
        }

        // Get the total Interest to be paid.
        totalInterest = 0D;
        totalPrincipal = 0D;
        for (LnSchedule lsr : lnSchedulesList) {
            totalInterest = totalInterest + lsr.getInterestAmount();
            totalPrincipal = totalPrincipal + lsr.getPrincipalAmount();
        }
        return lnSchedulesList;
    }

    @Transactional
    public TxnResult approveLoan(LnCreditApp request) {
        creditAppRepo.save(request);
        if (!request.getStatus().equals("ACTIVE"))
            return TxnResult.builder().message("approved").
                    code("00").data(request).build();

        DateFormat dateFormat = new SimpleDateFormat("yymmddhhmmss");
        String loanNumber = dateFormat.format(Calendar.getInstance().getTime());
        LnAccount lnAccount = new LnAccount();
        lnAccount.setCreditApplId(request.getId());
        lnAccount.setApprovedAmount(request.getApplAmt());
        lnAccount.setLedgerBal(request.getApplAmt());
        lnAccount.setStartDate(request.getStartDate());
        lnAccount.setEndDate(request.getEndDate());
        lnAccount.setLoanNumber(loanNumber);
        lnAccount.setStatus("PENDING");
        lnAccount.setRepayPeriod(request.getRepayPeriod());
        lnAccount.setTerm(request.getRepayTerm());
        lnAccount.setCreatedBy(request.getCreatedBy());
        lnAccount.setCreateDate(request.getCreateDate());
        lnAccount.setRowVersion(0);
        Integer loanId = lnAccountService.save(lnAccount);
        lnAccount.setLoanId(loanId);

        // Get the Loan Repayment Information
        LnRepaymentInfo lnRepaymentInfo = new LnRepaymentInfo();
        lnRepaymentInfo.setRepaymentType("REDUCING_BALANCE");
        lnRepaymentInfo.setIntCalcOption("REDUCING_BALANCE");
        lnRepaymentInfo.setInterestRate(8D);
        List<LnSchedule> lnScheduleList = null;
        if (lnRepaymentInfo.getIntCalcOption().equals("REDUCING_BALANCE"))
            lnScheduleList = computeReducingBalanceSchedule(request, lnAccount, lnRepaymentInfo);
        else if (lnRepaymentInfo.getIntCalcOption().equals("FLAT_AMOUNT")) {
            lnScheduleList = computeFlatAmountSchedule(request, lnAccount, lnRepaymentInfo);
        }

        if (lnScheduleList.isEmpty() || lnScheduleList == null)
            return TxnResult.builder().message("System failed to generate schedules").
                    code("-99").data(request).build();
        // Save the Loan Repayment Schedules
        for (LnSchedule schedule : lnScheduleList) {
            loanScheduleRepo.save(schedule);
        }
        // Update the loan account information
        lnAccount.setRepayInterest(BigDecimal.valueOf(totalInterest));
        lnAccount.setRepayPrincipal(BigDecimal.valueOf(totalPrincipal));
        lnAccount.setEndDate(maturityDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
