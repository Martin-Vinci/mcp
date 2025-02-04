package com.greybox.mediums.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greybox.mediums.config.RestTemplateConfig;
import com.greybox.mediums.config.SchemaConfig;
import com.greybox.mediums.entities.TransCenteTrustSummary;
import com.greybox.mediums.entities.SystemParameter;
import com.greybox.mediums.inter_switch.dto.JSONDataTransform;
import com.greybox.mediums.inter_switch.utils.HttpUtil;
import com.greybox.mediums.models.CenteTrustExport;
import com.greybox.mediums.models.ErrorData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.SystemParameterRepo;
import com.greybox.mediums.repository.TransCenteTrustRepo;
import com.greybox.mediums.repository.TransCenteTrustSummaryRepo;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.Logger;
import com.greybox.mediums.utils.MediumException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static com.greybox.mediums.utils.Logger.logError;
import static com.greybox.mediums.utils.Logger.logInfo;

@Service
public class CenteTrustService {

    @Autowired
    private SchemaConfig schemaConfig;

    @Autowired
    private TransCenteTrustRepo transCenteTrustRepo;

    @Autowired
    private SystemParameterRepo systemParameterRepo;

    @Autowired
    private TransCenteTrustSummaryRepo transCenteTrustSummaryRepo;

    @Autowired
    private EquiWebService equiWebService;

    public void postPendingCenteTransactions() {
        try {
            List<TransCenteTrustSummary> pendingTransactions = transCenteTrustRepo.findPendingCenteTrustTransactions();
            if (pendingTransactions == null || pendingTransactions.isEmpty()) {
                return;
            }

            SystemParameter trustAccount = systemParameterRepo.findParameterByCode("S26");
            SystemParameter micropayOperationAccount = systemParameterRepo.findParameterByCode("S27");

            validateSystemParameters(trustAccount, micropayOperationAccount);

            String baseUrl = schemaConfig.getMcpGateWayURL() + "/BillPayment/crdbPostTransaction";
            logError("==== Starting Cente Trust Account Postings ====");

            for (TransCenteTrustSummary transaction : pendingTransactions) {
                prepareTransactionAccounts(transaction, trustAccount, micropayOperationAccount);
                transaction.setMainTransId(transCenteTrustRepo.getTransactionRef());

                String response = HttpUtil.postHTTPRequest(baseUrl, new HashMap<>(), JSONDataTransform.marshall(transaction));
                processTransactionResponse(response, transaction);
            }

            logError("==== Completed Cente Trust Account Postings ====");
        } catch (Exception e) {
            logError("Exception during Cente Trust transaction posting: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateSystemParameters(SystemParameter trustAccount, SystemParameter micropayOperationAccount) throws MediumException {
        if (trustAccount == null || trustAccount.getParamValue() == null) {
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Trust Account is not configured. Please contact system admin.")
                    .build());
        }

        if (micropayOperationAccount == null || micropayOperationAccount.getParamValue() == null) {
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Micropay Operation Account is not configured. Please contact system admin.")
                    .build());
        }
    }

    private void prepareTransactionAccounts(TransCenteTrustSummary transaction, SystemParameter trustAccount, SystemParameter micropayOperationAccount) {
        if ("TRUST_TO_MICROPAY".equals(transaction.getEntryType().trim())) {
            transaction.setDrAcctNo(trustAccount.getParamValue());
            transaction.setCrAcctNo(micropayOperationAccount.getParamValue());
        } else {
            transaction.setDrAcctNo(micropayOperationAccount.getParamValue());
            transaction.setCrAcctNo(trustAccount.getParamValue());
        }
    }

    private void processTransactionResponse(String response, TransCenteTrustSummary transaction) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        CenteTrustExport transactionResponse = objectMapper.readValue(response, CenteTrustExport.class);

        if (transactionResponse == null || !"0".equals(transactionResponse.getResponse().getResponseCode())) {
            logError("Trust Posting Failed with MCP Gateway Response: " + response);
            transaction.setStatus("PENDING");
            transaction.setMessage(transactionResponse != null ? transactionResponse.getResponse().getResponseMessage() : "Unknown error");
        } else {
            updateTransactionBalances(transaction, transactionResponse);
            transaction.setStatus("PROCESSED");
            transaction.setMessage("SUCCESS");
            transaction.setPostingDate(DataUtils.getCurrentTimeStamp().toLocalDateTime());
            transCenteTrustSummaryRepo.save(transaction);
        }

        transCenteTrustRepo.updatePostedTrustTransaction(
                transaction.getEntryType(),
                transaction.getReferenceNo(),
                transaction.getBatchId(),
                transaction.getStatus(),
                transaction.getMessage()
        );
    }

    private void updateTransactionBalances(TransCenteTrustSummary transaction, CenteTrustExport response) {
        if ("TRUST_TO_MICROPAY".equals(transaction.getEntryType())) {
            transaction.setCrdbEscrowAcctBal(response.getDrAcctBal());
            transaction.setCrdbOppAcctBal(response.getCrAcctBal());
        } else {
            transaction.setCrdbEscrowAcctBal(response.getCrAcctBal());
            transaction.setCrdbOppAcctBal(response.getDrAcctBal());
        }
    }
}
