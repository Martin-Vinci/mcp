package com.greybox.mediums.services.mobile_loan;

import com.greybox.mediums.config.SchemaConfig;
import com.greybox.mediums.entities.mobile_loan.LnAccount;
import com.greybox.mediums.entities.mobile_loan.LnCreditApp;
import com.greybox.mediums.entities.mobile_loan.LnCustomer;
import com.greybox.mediums.entities.mobile_loan.LnSchedule;
import com.greybox.mediums.models.ErrorData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.mobile_loan.*;
import com.greybox.mediums.services.yoUganda.YoPaymentsAPIClient;
import com.greybox.mediums.services.yoUganda.YoPaymentsResponse;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.MediumException;
import com.greybox.mediums.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class LnAccountService {

    @Autowired
    private LoanAccountRepo loanAccountRepo;
    @Autowired
    private CreditAppRepo creditAppRepo;
    @Autowired
    private CustomerRepo customerRepo;
    @Autowired
    private LoanScheduleRepo loanScheduleRepo;
    @Autowired
    private SchemaConfig schemaConfig;


    public TxnResult findAll(LnAccount request) {
        List<LnAccount> lnAccountList;
        if (request.getStatus() != null)
            lnAccountList = loanAccountRepo.findLoansByStatus(request.getStatus());
        else if (request.getCustId() != null)
            lnAccountList = loanAccountRepo.findAll(request.getCustId());
        else
            lnAccountList = loanAccountRepo.findAll();
        if (lnAccountList == null || lnAccountList.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(lnAccountList).build();
    }


    public Integer save(LnAccount request) {
        request.setCreateDate(DataUtils.getCurrentDate().toLocalDate());
        LnAccount response = loanAccountRepo.save(request);
        return response.getLoanId();
    }

    public TxnResult update(LnAccount request) {
        loanAccountRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    @Transactional
    public TxnResult disburseLoan(LnAccount request) throws Exception {
        LnAccount loanAccount = loanAccountRepo.findLoanAccount(request.getLoanId());
        if (loanAccount == null)
            return TxnResult.builder().message("Invalid Loan account specified").
                    code("404").data(request).build();

        LnCreditApp creditAppl = creditAppRepo.findCreditAppl(loanAccount.getCreditApplId());
        if (creditAppl == null)
            return TxnResult.builder().message("Invalid credit application Id specified on this loan account").
                    code("404").data(request).build();

        LnCustomer customer = customerRepo.findCustById(creditAppl.getCustId());
        if (customer == null)
            return TxnResult.builder().code("404")
                    .message("Invalid customer Id specified")
                    .build();

        String mobilePhone = StringUtil.formatPhoneNumber(customer.getPhoneNo().trim());
        YoPaymentsAPIClient yoPaymentsClient = new YoPaymentsAPIClient(schemaConfig.getYoUgandaAPIUserName(), schemaConfig.getYoUgandaAPIPassword());
        String inputXML = yoPaymentsClient.createWithdrawalXml(loanAccount.getLedgerBal(), mobilePhone,
                "Loan disbursement [" + mobilePhone + "]");
        YoPaymentsResponse yoPaymentsResponse = yoPaymentsClient
                .executeYoPaymentsRequest(inputXML, schemaConfig.getYoUgandaPaymentURL());

        if (yoPaymentsResponse.getStatusCode() != 0)
            throw new MediumException(ErrorData.builder()
                    .code(String.valueOf(yoPaymentsResponse.getStatusCode()))
                    .message("Transaction failed @YoUganda").build());

        LnSchedule schedule = loanScheduleRepo.findMinimumUnPaidSchedule(loanAccount.getLoanId());
        if (schedule != null) {
            loanAccount.setNextPmtAmount(schedule.getInterestUnpaid() + schedule.getPrincipalUnpaid());
            loanAccount.setNextPmtDate(schedule.getDueDate());
        }
        loanAccount.setStatus("ACTIVE");
        loanAccountRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();

    }
}
