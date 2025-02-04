package com.greybox.mediums.services;

import com.greybox.mediums.entities.*;
import com.greybox.mediums.models.*;
import com.greybox.mediums.models.equiweb.CIAccount;
import com.greybox.mediums.models.equiweb.CIStatementRequest;
import com.greybox.mediums.utils.CommonService;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.MediumException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

@Service
public class AgentBankingService {
    @Autowired
    VoucherTransactionService voucherTransactionService;
    @Autowired
    CashOutInitiationService cashOutInitiationService;
    @Autowired
    OutletWithdrawInitiationService outletWithdrawInitiationService;
    @Autowired
    TransactionService transactionService;
    @Autowired
    MobileUserService mobileUserService;
    @Autowired
    PaymentRequestService paymentRequestService;
    @Autowired
    BillerNotifyService billerNotifyService;
    @Autowired
    MessageService messageService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private ReceiptService receiptService;
    @Autowired
    EscrowService escrowService;


    public TxnResult validateVoucher(VoucherRequestData request) throws MediumException {
        TxnResult txnResult = this.voucherTransactionService.validateVoucher(request);
        return txnResult;
    }

    @Transactional
    public TxnResult buyVoucher(VoucherRequestData request) throws MediumException, NoSuchAlgorithmException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(new OutletAuthRequest(),
                request.getApiUserName(), request.getDeviceId(), request.getUserPhoneNo(), request.getOutletCode(),
                request.getPinNo(), request.getChannelCode());

        this.mobileUserService.internalPinAuthentication(outletAuthRequest);
        TxnResult txnResult = this.voucherTransactionService.buyVoucher(request);
        return txnResult;
    }

    @Transactional
    public TxnResult redeemVoucher(VoucherRequestData request) throws MediumException, NoSuchAlgorithmException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(new OutletAuthRequest(),
                request.getApiUserName(), request.getDeviceId(), request.getUserPhoneNo(), request.getOutletCode(),
                request.getPinNo(), request.getChannelCode());

        this.mobileUserService.internalPinAuthentication(outletAuthRequest);
        return this.voucherTransactionService.redeemVoucher(request);
    }

    public TxnResult fundsTransfer(TransRequestData request) throws NoSuchAlgorithmException, MediumException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(new OutletAuthRequest(),
                request.getApiUserName(), request.getDeviceId(), request.getUserPhoneNo(), request.getOutletCode(),
                request.getPinNo(), request.getChannelCode());

        this.channelService.validateChannelConfiguration(request.getChannelCode(),
                request.getApiUserName(), request.getApiPassword());

        this.mobileUserService.internalPinAuthentication(outletAuthRequest);
        return this.transactionService.fundsTransfer(request);
    }

    public TxnResult logTransaction(TransRequestData request) throws NoSuchAlgorithmException, MediumException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(new OutletAuthRequest(),
                request.getApiUserName(), request.getDeviceId(), request.getUserPhoneNo(), request.getOutletCode(),
                request.getPinNo(), request.getChannelCode());

        this.channelService.validateChannelConfiguration(request.getChannelCode(),
                request.getApiUserName(), request.getApiPassword());

        this.mobileUserService.internalPinAuthentication(outletAuthRequest);
        return this.transactionService.logTransaction(request);
    }

    public TxnResult completeTransaction(TransRequestData request) throws NoSuchAlgorithmException, MediumException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(new OutletAuthRequest(),
                request.getApiUserName(), request.getDeviceId(), request.getUserPhoneNo(), request.getOutletCode(),
                request.getPinNo(), request.getChannelCode());

        this.channelService.validateChannelConfiguration(request.getChannelCode(),
                request.getApiUserName(), request.getApiPassword());

        this.mobileUserService.internalPinAuthentication(outletAuthRequest);
        return this.transactionService.completeTransaction(request);
    }

    public TxnResult doAccountFullStatement(CIStatementRequest request) throws NoSuchAlgorithmException, MediumException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(new OutletAuthRequest(),
                request.getApiUserName(), request.getDeviceId(), request.getUserPhoneNo(), request.getOutletCode(),
                request.getPinNo(), request.getChannelCode());

        this.channelService.validateChannelConfiguration(request.getChannelCode(),
                request.getApiUserName(), request.getApiPassword());

        this.mobileUserService.internalPinAuthentication(outletAuthRequest);
        return this.transactionService.findAccountStatement(request);
    }

    public TxnResult doAccountBalance(AccountRequest request) throws NoSuchAlgorithmException, MediumException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(new OutletAuthRequest(),
                request.getApiUserName(), request.getDeviceId(), request.getUserPhoneNo(), request.getOutletCode(),
                request.getPinNo(), request.getChannelCode());

        this.channelService.validateChannelConfiguration(request.getChannelCode(),
                request.getApiUserName(), request.getApiPassword());

        this.mobileUserService.internalPinAuthentication(outletAuthRequest);
        return this.transactionService.doAccountBalance(request);
    }

    @Transactional
    public TxnResult saveEscrowTransaction(TransRequestData request) throws NoSuchAlgorithmException, MediumException {
        return this.escrowService.saveEscrowTransaction(request);
    }

    @Transactional
    public TxnResult approveEscrowTransaction(EscrowPendingTrans request) throws Exception {
        TxnResult txnResult = this.escrowService.approveEscrowTransaction(request);
        return txnResult;
    }

    @Transactional
    public TxnResult reverseTrans(TransReversalData request) throws MediumException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(new OutletAuthRequest(),
                request.getApiUserName(), request.getDeviceId(), request.getUserPhoneNo(), request.getOutletCode(),
                request.getPinNo(), request.getChannelCode());

        TxnResult txnResult = this.transactionService.reverseTrans(request);
        return txnResult;
    }

    @Transactional
    public TxnResult enrollCustomerByAgent(MobileUser request) throws MediumException {
        TxnResult txnResult = this.mobileUserService.enrollCustomerByAgent(request);
        return txnResult;
    }

    public TxnResult pinAuthentication(OutletAuthRequest request) throws NoSuchAlgorithmException, MediumException, URISyntaxException {
        return this.mobileUserService.pinAuthentication(request);
    }

    public TxnResult performDevicePairing(MobileUser request) throws NoSuchAlgorithmException, MediumException, URISyntaxException {
        return this.mobileUserService.performDevicePairing(request);
    }

    public TxnResult generateDeviceActivationCode(MobileUser request) throws MediumException {
        return this.mobileUserService.generateDeviceActivationCode(request);
    }

    public TxnResult pairCustomerDevice(OutletAuthRequest request) throws NoSuchAlgorithmException, MediumException, URISyntaxException {
        return this.mobileUserService.pairCustomerDevice(request);
    }

    public TxnResult logSMS(MessageOutbox request) throws NoSuchAlgorithmException, MediumException, URISyntaxException {
        this.messageService.logSMS(request);
        return TxnResult.builder().message("approved").code("00").build();
    }

    @Transactional
    public TxnResult updateBillNotificationStatus(BillerNotif request) throws MediumException, NoSuchAlgorithmException {
        TxnResult txnResult = this.billerNotifyService.updateBillStatus(request);
        return txnResult;
    }

    @Transactional
    public TxnResult logBillNotification(BillerNotif request) throws MediumException {
        request.setTransDate(DataUtils.getCurrentTimeStamp().toLocalDateTime());
        TxnResult txnResult = this.billerNotifyService.save(request);
        return txnResult;
    }


    @Transactional
    public TxnResult pinChange(PinRequestData request) throws MediumException, NoSuchAlgorithmException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(
                new OutletAuthRequest(), request.getApiUserName(), request.getDeviceId(),
                request.getUserPhoneNo(), request.getOutletCode(), request.getPinNo(),
                request.getChannelCode()
        );

        return this.mobileUserService.pinChange(request);
    }

    public TxnResult findAccountsByPhoneNo(AccountRequest request) throws MediumException, NoSuchAlgorithmException {
        return this.mobileUserService.accountValidationByPhoneNo(request.getPhoneNo());
    }

    public TxnResult findOutletDetails(OutletAuthRequest requestData) throws MediumException, NoSuchAlgorithmException {
        return this.mobileUserService.findOutletDetails(requestData);
    }

    public TxnResult findSuperAgentDetails(OutletAuthRequest requestData) throws MediumException, NoSuchAlgorithmException {
        return this.mobileUserService.findSuperAgentDetails(requestData);
    }

    public TxnResult findRecipientSMS(String phoneNo) throws MediumException, NoSuchAlgorithmException {
        return this.messageService.findRecipientSMS(phoneNo);
    }

    public TxnResult findTransCharges(TransRequestData request) throws MediumException {
        EntityAmount entityAmount = this.transactionService.findCharges(request);
        return TxnResult.builder()
                .data(entityAmount)
                .code("00")
                .message("Approved")
                .build();
    }

    @Transactional
    public TxnResult initiateCustomerCashout(CashOutRequest request) throws MediumException, NoSuchAlgorithmException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(
                new OutletAuthRequest(), request.getApiUserName(), request.getDeviceId(),
                request.getUserPhoneNo(), request.getOutletCode(), request.getPinNo(),
                request.getChannelCode()
        );

        this.mobileUserService.internalPinAuthentication(outletAuthRequest);
        return this.cashOutInitiationService.initiateCashout(request);
    }

    @Transactional
    public TxnResult validateCustomerCashoutOTP(CashOutRequest request) throws MediumException, NoSuchAlgorithmException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(
                new OutletAuthRequest(), request.getApiUserName(), request.getDeviceId(),
                request.getUserPhoneNo(), request.getOutletCode(), request.getPinNo(),
                request.getChannelCode()
        );

        PINAuthData pinAuthData = new PINAuthData();
        pinAuthData.setPhoneNo(request.getUserPhoneNo());
        pinAuthData.setPinNo(request.getPinNo());

        return this.cashOutInitiationService.validateCashoutOTP(request);
    }

    @Transactional
    public TxnResult initiateOutletCashout(CashOutRequest request) throws MediumException, NoSuchAlgorithmException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(
                new OutletAuthRequest(), request.getApiUserName(), request.getDeviceId(),
                request.getUserPhoneNo(), request.getOutletCode(), request.getPinNo(),
                request.getChannelCode()
        );

        this.mobileUserService.internalPinAuthentication(outletAuthRequest);
        return this.outletWithdrawInitiationService.initiateCashout(request);
    }

    @Transactional
    public TxnResult validateOutletCashoutOTP(CashOutRequest request) throws MediumException, NoSuchAlgorithmException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(
                new OutletAuthRequest(), request.getApiUserName(), request.getDeviceId(),
                request.getUserPhoneNo(), request.getOutletCode(), request.getPinNo(),
                request.getChannelCode()
        );

        PINAuthData pinAuthData = new PINAuthData();
        pinAuthData.setPhoneNo(request.getUserPhoneNo());
        pinAuthData.setPinNo(request.getPinNo());

        return this.outletWithdrawInitiationService.validateCashoutOTP(request);
    }

    public TxnResult accountInquiryByPhoneNo(CIAccount request) throws MediumException {
        return this.mobileUserService.accountInquiryByPhoneNo(request);
    }

    public TxnResult createCustomer(CustomerDetail request) throws NoSuchAlgorithmException, MediumException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(
                new OutletAuthRequest(), request.getApiUserName(), request.getDeviceId(),
                request.getUserPhoneNo(), request.getOutletCode(), request.getPinNo(),
                request.getChannelCode()
        );

        this.channelService.validateChannelConfiguration(request.getChannelCode(),
                request.getApiUserName(), request.getApiPassword());

        this.mobileUserService.internalPinAuthentication(outletAuthRequest);
        return this.mobileUserService.createCustomer(request);
    }

    @Transactional
    public TxnResult saveReceipt(IssuedReceipt request) {
        return this.receiptService.save(request);
    }

    public TxnResult findIssuedReceipt(IssuedReceipt request) {
        return this.receiptService.find(request);
    }

    @Transactional
    public TxnResult requestPayment(PaymentRequest request) throws MediumException, NoSuchAlgorithmException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(
                new OutletAuthRequest(), request.getApiUserName(), request.getDeviceId(),
                request.getUserPhoneNo(), request.getOutletCode(), request.getPinNo(),
                request.getChannelCode()
        );

        this.mobileUserService.internalPinAuthentication(outletAuthRequest);
        return this.paymentRequestService.requestPayment(request);
    }

    @Transactional
    public TxnResult updatePaymentRequest(PaymentRequest request) throws MediumException, NoSuchAlgorithmException {
        OutletAuthRequest outletAuthRequest = CommonService.getCommonRequest(
                new OutletAuthRequest(), request.getApiUserName(), request.getDeviceId(),
                request.getUserPhoneNo(), request.getOutletCode(), request.getPinNo(),
                request.getChannelCode()
        );

        this.mobileUserService.internalPinAuthentication(outletAuthRequest);
        return this.paymentRequestService.updatePaymentRequest(request);
    }

    public TxnResult findPendingRequest(PaymentRequest request) {
        return this.paymentRequestService.findPendingRequest(request);
    }

    public TxnResult findRequestedPayments(PaymentRequest request) {
        return this.paymentRequestService.findRequestedPayments(request);
    }

}