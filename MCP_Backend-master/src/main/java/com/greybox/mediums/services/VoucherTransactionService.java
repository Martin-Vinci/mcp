package com.greybox.mediums.services;

import com.greybox.mediums.entities.TransactionDetail;
import com.greybox.mediums.entities.TransactionRef;
import com.greybox.mediums.entities.TransactionVoucher;
import com.greybox.mediums.models.*;
import com.greybox.mediums.repository.TransactionDetailRepo;
import com.greybox.mediums.repository.TransactionRefRepo;
import com.greybox.mediums.repository.TransactionVoucherRepo;
import com.greybox.mediums.utils.CommonService;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.MediumException;
import com.greybox.mediums.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.greybox.mediums.utils.Logger.logError;
import static com.greybox.mediums.utils.Logger.logInfo;

@Service
public class VoucherTransactionService {
    @Autowired
    private TransactionVoucherRepo transactionVoucherRepo;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionRefRepo transactionRefRepo;
    @Autowired
    private TransactionDetailRepo transactionDetailRepo;
    @Autowired
    private EquiWebService equiWebService;

    public TxnResult findTransVouchers(TransactionVoucher request) {
        List<TransactionVoucher> customers = transactionVoucherRepo.findTransVouchers();
        if (customers == null)
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult validateVoucher(VoucherRequestData request) throws MediumException {
        TransactionVoucher voucher = transactionVoucherRepo.findVoucherByRecipientPhone
                (request.getVoucherNo(), StringUtil.formatPhoneNumber(request.getRecipientPhone()));
        if (voucher == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid voucher code or recipient phone number specified").build());
        VoucherResponse response = new VoucherResponse();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        response.setExpiryDate(formatter.format(voucher.getExpiryDate()));
        response.setVoucherNo(voucher.getVoucherCode());
        response.setRecipientPhoneNo(voucher.getRecipientPhoneNo());
        response.setSourcePhoneNo(voucher.getSourcePhoneNo());
        response.setTransAmount(voucher.getAmount());
        response.setDescription(voucher.getNarration());
        Duration diff = Duration.between(voucher.getExpiryDate(), DataUtils.getCurrentTimeStamp().toLocalDateTime());
        long seconds = diff.getSeconds();

        if (seconds > 0)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Voucher code is expired").build());

        response.setStatus(seconds > 0 ? "EXPIRED" : "PENDING");
        return TxnResult.builder().message("approved").
                code("00").data(response).build();
    }

    public TxnResult buyVoucher(VoucherRequestData request) throws MediumException {
        TransactionVoucher transactionVoucher = new TransactionVoucher();
        TransRequestData transRequestData;
        transRequestData = (TransRequestData) CommonService.getCommonRequest(new TransRequestData(),
                request.getApiUserName(), request.getDeviceId(), request.getUserPhoneNo(), request.getOutletCode(),
                request.getPinNo(), request.getChannelCode());
        transRequestData.setCurrency("UGX");
        transRequestData.setTransAmt(request.getTransAmt());
        transRequestData.setServiceCode(70018);
        transRequestData.setSourceAcctNo(request.getSourceAcct());
        transRequestData.setDescription(request.getDescription());
        TxnResult txnResult = transactionService.fundsTransfer(transRequestData);
        if (!txnResult.getCode().equals("00"))
           return txnResult;

        TransRespData txnResp = (TransRespData) txnResult.getData();
        transactionVoucher.setBuyTransId(txnResp.getTransId());
        transactionVoucher.setAmount(request.getTransAmt());
        transactionVoucher.setCreateDate(DataUtils.getCurrentDate().toLocalDate());
        transactionVoucher.setSourcePhoneNo(request.getSourcePhoneNo());
        transactionVoucher.setRecipientPhoneNo(request.getRecipientPhone());
        transactionVoucher.setNarration(request.getDescription());
        transactionVoucher.setStatus("PENDING");
        transactionVoucher.setExpiryDate(DataUtils.getCurrentTimeStamp().toLocalDateTime().plusDays(5));
        DateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        String voucherCode = dateFormat.format(Calendar.getInstance().getTime()) + "" + StringUtil.generateRandomNumber(4);
        transactionVoucher.setVoucherCode(voucherCode);
        transactionVoucherRepo.save(transactionVoucher);
        transactionVoucher.setTransRespData((TransRespData) txnResult.getData());
        return TxnResult.builder().message("approved").
                code("00").data(transactionVoucher).build();
    }

    public TxnResult redeemVoucher(VoucherRequestData request) throws MediumException, NoSuchAlgorithmException {
        TransactionVoucher transactionVoucher = transactionVoucherRepo.findActiveVoucher(request.getVoucherNo(), StringUtil.formatPhoneNumber(request.getRecipientPhone()));
        if (transactionVoucher == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid voucher code or recipient phone number specified").build());

        TransRequestData transRequestData;
        transRequestData = (TransRequestData) CommonService.getCommonRequest(new TransRequestData(),
                request.getApiUserName(), request.getDeviceId(), request.getUserPhoneNo(), request.getOutletCode(),
                request.getPinNo(), request.getChannelCode());
        transRequestData.setCurrency("UGX");
        transRequestData.setTransAmt(transactionVoucher.getAmount());
        transRequestData.setServiceCode(70019);
        transRequestData.setTransAmt(transactionVoucher.getAmount());
        transRequestData.setDestAcctNo(request.getOutletAcctNo());
        transRequestData.setDescription(transactionVoucher.getNarration());
        TxnResult txnResult = transactionService.fundsTransfer(transRequestData);
        if (!txnResult.getCode().equals("00"))
            throw new MediumException(ErrorData.builder()
                    .code(txnResult.getCode())
                    .message(txnResult.getMessage()).build());
        TransRespData txnResp = (TransRespData) txnResult.getData();

        transactionVoucher.setRedeemTransId(txnResp.getTransId());
        transactionVoucher.setStatus("PROCESSED");
        transactionVoucherRepo.save(transactionVoucher);
        return txnResult;
    }

    @Transactional
    public void processVoucherExpiryService() throws MediumException {
        ArrayList<TransactionVoucher> transactionVouchers = transactionVoucherRepo.findActiveVouchers();
        if (transactionVouchers == null || transactionVouchers.isEmpty()) {
            logInfo("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Transaction voucher: No Active Vouchers exists in the System");
            return;
        }
        List<TransactionDetail> transactionDetailList;
        List<TransDetails> transDetailsList;
        TransRequestData transRequestData;


        DateTimeFormatter localDateFormater = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        for (TransactionVoucher transactionVoucher : transactionVouchers) {
            try {
                Duration diff = Duration.between(transactionVoucher.getExpiryDate(), DataUtils.getCurrentTimeStamp().toLocalDateTime());
                long seconds = diff.getSeconds();
                if (seconds < 0)
                    continue;

                logInfo("########################### Transaction voucher is expired: Voucher number: " + transactionVoucher.getVoucherCode() + ", Current date: "
                        + formatter.format(DataUtils.getCurrentTimeStamp()) + ". Expiry date: " + localDateFormater.format(transactionVoucher.getExpiryDate()));
                logInfo("%%%%%%%%%%%%%%%%%%%%%%%%%%% Proceeding to automatic reversal" + transactionVoucher.getVoucherCode() + ", Current date: "
                        + formatter.format(DataUtils.getCurrentTimeStamp()) + ". Expiry date: " + localDateFormater.format(transactionVoucher.getExpiryDate()));

                transactionDetailList = transactionDetailRepo.findPendingTransactionDetails(transactionVoucher.getBuyTransId());
                if (transactionDetailList == null || transactionDetailList.isEmpty()) {
                    logInfo("########################### Transaction voucher. No Transaction details were found for transaction voucher reference: " + transactionVoucher.getBuyTransId() + ", Current date: "
                            + formatter.format(DataUtils.getCurrentTimeStamp()) + ". Expiry date: " + localDateFormater.format(transactionVoucher.getExpiryDate()));
                    continue;
                }

                TransactionRef transactionDetail = transactionRefRepo.findTransactionRef(transactionVoucher.getBuyTransId());
                if (transactionDetail == null) {
                    logInfo("########################### Transaction voucher. Could not locate Base transaction for voucher reference: " + transactionVoucher.getBuyTransId() + ", Current date: "
                            + formatter.format(DataUtils.getCurrentTimeStamp()) + ". Expiry date: " + localDateFormater.format(transactionVoucher.getExpiryDate()));
                    continue;
                }
                transDetailsList = new ArrayList<>();
                transRequestData = new TransRequestData();

                transRequestData.setSourceAcctNo(transactionDetail.getCrAcctNo().trim());
                transRequestData.setDestAcctNo(transactionDetail.getDrAcctNo().trim());

                for (int i = 0; i < transactionDetailList.size(); i++) {
                    logInfo(transactionDetailList.get(i));
                    if (transactionDetailList.get(i).getItemNo().equals(1)) {
                        transDetailsList.add(TransDetails.builder()
                                .transAmt(transactionDetailList.get(i).getAmount())
                                .sourceAcctNo(transactionDetailList.get(i).getCrAcctNo())
                                .destAcctNo(transactionDetailList.get(i).getDrAcctNo())
                                .description("Voucher Reversal=>" + transactionVoucher.getVoucherCode().trim() + "=>" + transactionVoucher.getNarration().trim())
                                .transType(StringUtil.getTransferType( // The accounts have been switched because we want to reverse Voucher
                                        transactionDetailList.get(i).getCrAcctNo(),
                                        transactionDetailList.get(i).getDrAcctNo()
                                ))
                                .build());
                    }
                }
                TxnResult transactionResponse = equiWebService.fundsTransfer(transRequestData, transDetailsList, false);
                if (!transactionResponse.getCode().equals("00")) {
                    logInfo("########################### Transaction voucher. " + transactionResponse.getMessage() + ". " + transactionVoucher.getBuyTransId() + ", Current date: "
                            + formatter.format(DataUtils.getCurrentTimeStamp()) + ". Expiry date: " + localDateFormater.format(transactionVoucher.getExpiryDate()));
                    continue;
                }
                transactionVoucher.setStatus("REVERSED");
                transactionVoucherRepo.save(transactionVoucher);

            } catch (Exception e) {
                logError(e);
            }
        }
        return;
    }
}
