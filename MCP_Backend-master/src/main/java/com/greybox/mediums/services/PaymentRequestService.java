package com.greybox.mediums.services;

import com.greybox.mediums.entities.MobileUserAccount;
import com.greybox.mediums.entities.PaymentRequest;
import com.greybox.mediums.models.TransRequestData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.MobileUserAccountRepo;
import com.greybox.mediums.repository.PaymentRequestRepo;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.StringUtil;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentRequestService {
    @Autowired
    private PaymentRequestRepo paymentRequestRepo;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private MobileUserAccountRepo mobileUserAccountRepo;

    public TxnResult findPendingRequest(PaymentRequest request) {
        List<PaymentRequest> customers = this.paymentRequestRepo.findPendingRequest(request.getFromPhone());
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404").message("No records found").build();
        return TxnResult.builder().message("approved").code("00").data(customers).build();
    }

    public TxnResult findRequestedPayments(PaymentRequest request) {
        List<PaymentRequest> customers = this.paymentRequestRepo.findRequestedPayments(request.getFromPhone());
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404").message("No records found").build();
        return TxnResult.builder().message("approved").code("00").data(customers).build();
    }

    public TxnResult requestPayment(PaymentRequest request) {
        request.setFromPhone(StringUtil.formatPhoneNumber(request.getFromPhone()));
        request.setRequesterPhone(StringUtil.formatPhoneNumber(request.getRequesterPhone()));
        request.setCreateDate(DataUtils.getCurrentTimeStamp().toLocalDateTime());
        request.setStatus("Pending");
        this.paymentRequestRepo.save(request);
        return TxnResult.builder().message("approved").code("00").data(request).build();
    }

    @Transactional
    public TxnResult updatePaymentRequest(PaymentRequest request) {
        if (!request.getAction().equals("Approved") && !request.getAction().equals("Declined")) {
            return TxnResult.builder().message("Invalid action specified").code("00").data(request).build();
        }
        PaymentRequest paymentRequest = this.paymentRequestRepo.findRequestedPayments(request.getRequestId());
        if (paymentRequest == null) {
            return TxnResult.builder().message("Invalid request Id specified").code("00").data(request).build();
        }
        paymentRequest.setModifyDate(DataUtils.getCurrentDate().toLocalDate());
        paymentRequest.setStatus(request.getAction());

        if (request.getAction().equals("Declined")) {
            this.paymentRequestRepo.save(paymentRequest);
            return TxnResult.builder().message("approved").code("00").data(request).build();
        }

        MobileUserAccount recipientAccount = this.mobileUserAccountRepo.findUserAccountByPhoneNumber(paymentRequest.getRequesterPhone());
        if (recipientAccount == null) {
            return TxnResult.builder().message("Could not locate recipient account number").code("00").data(request).build();
        }
        MobileUserAccount drawerAccount = this.mobileUserAccountRepo.findUserAccountByPhoneNumber(paymentRequest.getFromPhone());
        if (drawerAccount == null) {
            return TxnResult.builder().message("Could not locate source account number").code("00").data(request).build();
        }
        this.paymentRequestRepo.save(paymentRequest);

        TransRequestData requestData = new TransRequestData();
        requestData.setOutletCode("CUSTOMER");
        requestData.setChannelCode("MOBILE");
        requestData.setDestAcctNo(recipientAccount.getAcctNo());
        requestData.setTransAmt(paymentRequest.getAmount());
        requestData.setCurrency("UGX");
        requestData.setSourceAcctNo(drawerAccount.getAcctNo());
        requestData.setDescription("Payment Request From [" + paymentRequest.getRequesterPhone() + "] Reason [" + paymentRequest.getRequesterReason() + "]");
        requestData.setServiceCode(Integer.valueOf(70002));
        requestData.setDepositorPhoneNo(paymentRequest.getFromPhone());
        TxnResult txnResult = this.transactionService.fundsTransfer(requestData);
        return txnResult;
    }
}
