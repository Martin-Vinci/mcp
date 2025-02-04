package com.greybox.mediums.services;

import com.greybox.mediums.config.SchemaConfig;
import com.greybox.mediums.entities.EscrowPendingTrans;
import com.greybox.mediums.entities.SystemParameter;
import com.greybox.mediums.entities.TransCenteTrust;
import com.greybox.mediums.models.*;
import com.greybox.mediums.repository.EscrowPendingTransRepo;
import com.greybox.mediums.repository.SystemParameterRepo;
import com.greybox.mediums.repository.TransCenteTrustRepo;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.MediumException;
import com.greybox.mediums.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class EscrowService {
    @Autowired
    private EscrowPendingTransRepo escrowPendingTransRepo;
    @Autowired
    private SystemParameterService systemParameterService;
    @Autowired
    private SchemaConfig schemaConfig;
    @Autowired
    private TransCenteTrustRepo transCenteTrustRepo;
    @Autowired
    private SystemParameterRepo systemParameterRepo;
    @Autowired
    private EquiWebService equiWebService;


    public TxnResult findEscrowTransactions(EscrowPendingTrans request) {
        List<EscrowPendingTrans> customers = escrowPendingTransRepo.findTransactions();
        if (customers == null)
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }


    @Transactional
    public TxnResult approveEscrowTransaction(EscrowPendingTrans request) {
        if (request.getAction().equals("APPROVED")) {
            TransRequestData transRequestData = new TransRequestData();
            transRequestData.setSourceAcctNo(request.getCrAcctNo().trim());
            transRequestData.setDestAcctNo(request.getDrAcctNo().trim());
            transRequestData.setChannelCode("BACKOFFICE");

            List<TransDetails> transDetailsList = new ArrayList<>();
            transDetailsList.add(TransDetails.builder()
                    .transAmt(request.getAmount())
                    .sourceAcctNo(request.getCrAcctNo())
                    .destAcctNo(request.getDrAcctNo())
                    .amountType("TRANS_AMOUNT")
                    .description("Super-Agent Float Purchase=>" + request.getExternalTransRef().trim())
                    .transType(StringUtil.getTransferType(request.getDrAcctNo(), request.getCrAcctNo()))
                    .build());
            TxnResult transactionResponse = equiWebService.fundsTransfer(transRequestData, transDetailsList, false);
            if (!transactionResponse.getCode().equals("00"))
                throw new MediumException(ErrorData.builder()
                        .code(transactionResponse.getCode())
                        .message(transactionResponse.getMessage()).build());

            request.setUtilPosted("Y");
            logCenteTrustTransaction(99999L, request.getAmount(), request.getPostedBy());
        } else {
            request.setUtilPosted("Y");
        }
        escrowPendingTransRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").build();
    }

    @Transactional
    public TxnResult saveEscrowTransaction(TransRequestData request) throws NoSuchAlgorithmException, MediumException {
        String debitAccount = systemParameterService.findParameterByCode("S17");
        EscrowPendingTrans transaction = new EscrowPendingTrans();
        transaction.setCrAcctNo(request.getDestAcctNo());
        transaction.setDrAcctNo(debitAccount);
        transaction.setAmount(request.getTransAmt());
        transaction.setIsoCode(request.getCurrency());
        transaction.setPostedBy("SYSTEM");
        transaction.setTransDescr(request.getDescription());
        transaction.setSuccessFlag("Y");
        transaction.setTransDate(DataUtils.getCurrentTimeStamp());
        transaction.setUtilPosted("N");
        transaction.setReversalFlag("N");
        transaction.setExternalTransRef(request.getExternalReference());
        EscrowPendingTrans response = escrowPendingTransRepo.save(transaction);
        TransRespData transRespData = new TransRespData();
        transRespData.setTransId(response.getTransId());
        return TxnResult.builder().message("approved").
                code("00").data(transRespData).build();
    }

    private void logCenteTrustTransaction(Long cbsTxnId, BigDecimal amount, String userName) {
        TransCenteTrust centeTrust;
        LocalDateTime createDate = DataUtils.getCurrentTimeStamp().toLocalDateTime();
        SystemParameter trustAccountParam, micropayOperationAccount;
        trustAccountParam = systemParameterRepo.findParameterByCode("S26");
        if (trustAccountParam == null || trustAccountParam.getParamValue() == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Trust Account is not configured. Please contact system admin").build());

        micropayOperationAccount = systemParameterRepo.findParameterByCode("S27");
        if (micropayOperationAccount == null || micropayOperationAccount.getParamValue() == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Trust Account is not configured. Please contact system admin").build());

        String referenceNo = (new SimpleDateFormat("yyyyMMdd")).format(new Date());

         centeTrust = new TransCenteTrust(cbsTxnId, micropayOperationAccount.getParamValue(), trustAccountParam.getParamValue(), amount, "Escrow_Super_Agent_Deposit", userName, "Pending", "999999", referenceNo);
        transCenteTrustRepo.save(centeTrust);
    }
}
