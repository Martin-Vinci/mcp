package com.greybox.mediums.services;

import com.google.gson.Gson;
import com.greybox.mediums.config.RestTemplateConfig;
import com.greybox.mediums.config.SchemaConfig;
import com.greybox.mediums.entities.CustomerDetail;
import com.greybox.mediums.models.*;
import com.greybox.mediums.models.equiweb.*;
import com.greybox.mediums.models.equiweb.AuthRequest;
import com.greybox.mediums.repository.TransactionDetailRepo;
import com.greybox.mediums.repository.TransactionRefRepo;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.Logger;
import com.greybox.mediums.utils.MediumException;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class EquiWebService {
    @Autowired
    private SchemaConfig schemaConfig;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired
    TransactionRefRepo transactionDetailRepo;
    @Autowired
    Gson gson;

    private ServiceResponseWrapper processServiceRequest(ServiceRequestWrapper request) throws Exception {
        RestTemplateConfig restTemplate = new RestTemplateConfig();
        String baseUrl = this.schemaConfig.getEquiwebWSClient() + "/ChannelIntegrator/processServiceRequest";
        JSONObject jsonObject = new JSONObject(request);
        String jsonRequest = jsonObject.toString();
        TxnResult wsResponse = restTemplate.post(jsonRequest, baseUrl, "POST", "eQuiWeb");
        if (!wsResponse.getCode().equals("00")) {
            return ServiceResponseWrapper.builder()
                    .responseCode(wsResponse.getCode())
                    .responseMessage(wsResponse.getMessage())
                    .build();
        }
        return this.gson.fromJson((String) wsResponse.getData(), ServiceResponseWrapper.class);
    }

    private AuthRequest getAuthorizeRequest() {
        return AuthRequest.builder()
                .channelCode("AGENT_BANKING")
                .institutionId(Integer.valueOf(this.schemaConfig.getEquiWebInstitutionId()))
                .vCode(this.schemaConfig.getEquiwebVendorCode())
                .vPassword(this.schemaConfig.getEquiwebVendorPassword())
                .userName(this.schemaConfig.getEquiwebUsername())
                .build();
    }

    private Long generateRequestId() throws Exception {
        ServiceRequestBody requestWrapper = new ServiceRequestBody();
        RequestInput dataWrapper = new RequestInput();
        requestWrapper.setServiceCode("GET_REQUEST_ID");
        requestWrapper.setRequestInput(dataWrapper);
        ServiceResponseWrapper response = processServiceRequest(ServiceRequestWrapper.builder()
                .authRequest(getAuthorizeRequest())
                .requestBody(requestWrapper)
                .build());
        if (!response.getResponseCode().equals("0")) {
            throw new MediumException(ErrorData.builder()
                    .code(response.getResponseCode())
                    .message(response.getResponseMessage()).build());
        }
        RequestOutput requestOutput = response.getRequestOutput();
        if (requestOutput.getOutputParameters() == null) {
            return Long.valueOf(0L);
        }
        OutputParameters outputItems = requestOutput.getOutputParameters();
        for (ExportItem item : outputItems.getExportItems()) {
            if (item.getCode().equalsIgnoreCase("REQUEST_ID"))
                return Long.valueOf(Long.parseLong(item.getValue()));
        }
        return null;
    }

    private TxnResult getRespMessage(ServiceResponseWrapper resp) {
        String responseCode = resp.getResponseCode();
        if (!responseCode.equals("0")) {
            return TxnResult.builder()
                    .code(responseCode)
                    .message("ERROR[" + resp.getResponseCode() + "]: " + resp.getResponseMessage() + " at eQuiWeb")
                    .build();
        }
        return TxnResult.builder()
                .code("00")
                .message("Approved")
                .build();
    }

    private TransRespData getTranResp(RequestOutput requestOutput) {
        TransRespData resp = new TransRespData();
        if (requestOutput.getOutputParameters() == null)
            return resp;
        for (ExportItem item : requestOutput.getOutputParameters().getExportItems()) {
            if (item.getCode().equalsIgnoreCase("DEBIT_ACCOUNT_BALANCE")) {
                resp.setDrAcctBalance(new BigDecimal(item.getValue()));
            }
            if (item.getCode().equalsIgnoreCase("CREDIT_ACCOUNT_BALANCE")) {
                resp.setCrAcctBalance(new BigDecimal(item.getValue()));
            }
            if (item.getCode().equalsIgnoreCase("TRANSACTION_ID")) {
                resp.setCbsTransId(item.getValue());
            }
        }
        return resp;
    }

    private CIAccountBalance getAccountBalanceResp(RequestOutput requestOutput) {
        CIAccountBalance resp = new CIAccountBalance();
        if (requestOutput.getOutputParameters() == null) {
            return resp;
        }
        for (ExportItem item : requestOutput.getOutputParameters().getExportItems()) {
            if (item.getCode().equalsIgnoreCase("ACCOUNT_TITLE")) {
                resp.setAccountTitle(item.getValue());
                continue;
            }
            if (item.getCode().equalsIgnoreCase("BRANCH_NAME")) {
                resp.setBranchName(item.getValue());
                continue;
            }
            if (item.getCode().equalsIgnoreCase("AVAILABLE_BALANCE")) {
                resp.setAvailableBalance(new BigDecimal(item.getValue()));
                continue;
            }
            if (item.getCode().equalsIgnoreCase("ACCOUNT_CURRENCY")) {
                resp.setCurrency(item.getValue());
                continue;
            }
            if (item.getCode().equalsIgnoreCase("ACCOUNT_STATUS")) {
                resp.setAccountStatus(item.getValue());
                continue;
            }
            if (item.getCode().equalsIgnoreCase("CUSTOMER_NUMBER")) {
                resp.setCustomerNo(item.getValue());
            }
        }
        return resp;
    }

    private CIAccount getAccountValidationResp(String accountNo, RequestOutput requestOutput) {
        CIAccount resp = new CIAccount();
        if (requestOutput.getOutputParameters() == null) {
            return resp;
        }
        resp.setAccountNo(accountNo);
        for (ExportItem item : requestOutput.getOutputParameters().getExportItems()) {
            if (item.getCode().equalsIgnoreCase("CUSTOMER_NAME")) {
                resp.setAcctTitle(item.getValue());
                continue;
            }
            if (item.getCode().equalsIgnoreCase("PHONE_NUMBER")) {
                resp.setPhoneNo(item.getValue());
                continue;
            }
            if (item.getCode().equalsIgnoreCase("ENTITY_TYPE")) {
                resp.setEntityType(item.getValue());
            }
        }

        if (requestOutput.getOutputParameterItems() == null) {
            return resp;
        }
        for (OutputParameterItem outputParameterItem : requestOutput.getOutputParameterItems()) {
            for (ExportItem item : outputParameterItem.getExportItems()) {
                if (item.getCode().equalsIgnoreCase("ACCOUNT_NUMBER")) {
                    resp.setAccountNo(item.getValue());
                }
            }
        }
        return resp;
    }

    private List<CIAccount> getCustomerAccounts(RequestOutput requestOutput) {
        List<CIAccount> accountList = null;
        CIAccount resp = new CIAccount();
        if (requestOutput.getOutputParameterItems() == null) {
            return null;
        }
        accountList = new ArrayList<>();
        for (OutputParameterItem outputParameterItem : requestOutput.getOutputParameterItems()) {
            for (ExportItem item : outputParameterItem.getExportItems()) {
                if (item.getCode().equalsIgnoreCase("ACCOUNT_TYPE")) {
                    resp.setAcctType(item.getValue());
                    continue;
                }
                if (item.getCode().equalsIgnoreCase("ACCOUNT_NUMBER")) {
                    resp.setAccountNo(item.getValue());
                    continue;
                }
                if (item.getCode().equalsIgnoreCase("DESCRIPTION")) {
                    resp.setAcctTitle(item.getValue());
                }
            }
            accountList.add(resp);
        }
        return accountList;
    }

    private CICustomer getPhoneValidationResp(RequestOutput requestOutput) {
        CICustomer resp = new CICustomer();
        if (requestOutput.getOutputParameters() == null) {
            return resp;
        }
        for (ExportItem item : requestOutput.getOutputParameters().getExportItems()) {
            if (item.getCode().equalsIgnoreCase("CUSTOMER_NAME")) {
                resp.setCustomerName(item.getValue());
                continue;
            }
            if (item.getCode().equalsIgnoreCase("PHONE_NUMBER")) {
                resp.setPhoneNo(item.getValue());
                continue;
            }
            if (item.getCode().equalsIgnoreCase("ENTITY_TYPE")) {
                resp.setEntityType(item.getValue());
            }
        }
        if (requestOutput.getOutputParameterItems() == null) {
            return resp;
        }
        List<CIAccount> accountList = new ArrayList<>();
        for (OutputParameterItem outputParameterItem : requestOutput.getOutputParameterItems()) {
            for (ExportItem item : outputParameterItem.getExportItems()) {
                CIAccount account = new CIAccount();
                if (item.getCode().equalsIgnoreCase("ACCOUNT_TYPE")) {
                    account.setAcctType(item.getValue());
                    continue;
                }
                if (item.getCode().equalsIgnoreCase("ACCOUNT_NUMBER")) {
                    account.setAccountNo(item.getValue());
                    continue;
                }
                if (item.getCode().equalsIgnoreCase("DESCRIPTION")) {
                    account.setAcctTitle(item.getValue());
                    continue;
                }
                accountList.add(account);
            }
        }
        if (!accountList.isEmpty())
            resp.setAccountList(accountList);
        return resp;
    }

    private List<CIStatementResponse> getAccountStatementResp(RequestOutput requestOutput, String statementType) {
        List<CIStatementResponse> transItems = new ArrayList<>();
        if (requestOutput.getTransOutput() == null) {
            return transItems;
        }
        if (statementType.equals("FULL")) {
            for (StatementOutput stmt : requestOutput.getTransOutput()) {
                CIStatementResponse item = new CIStatementResponse();
                item.setDescription(stmt.getDescription());
                item.setEffectiveDate(stmt.getTransDate());
                item.setTxnAmount(new BigDecimal(stmt.getTransAmount()));
                item.setClosing(new BigDecimal(stmt.getClosingBalance()));
                transItems.add(item);
            }
        } else {
            List<StatementOutput> transactions = requestOutput.getTransOutput();
            int numberOfRecords = Math.min(transactions.size(), 5);
            for (int i = 0; i < numberOfRecords; i++) {
                CIStatementResponse item = new CIStatementResponse();
                item.setDescription(transactions.get(i).getDescription().trim());
                item.setEffectiveDate(transactions.get(i).getTransDate());
                item.setTxnAmount(new BigDecimal(transactions.get(i).getTransAmount()));
                item.setClosing(new BigDecimal(transactions.get(i).getClosingBalance()));
                transItems.add(item);
            }
        }
        return transItems;
    }

    public TxnResult fundsTransfer(TransDetails transaction) {
        ServiceRequestBody serviceRequestBody = new ServiceRequestBody();
        List<Item> importParams = new ArrayList<>();
        try {
            importParams.add(Item.builder()
                    .code("CREDIT_ACCOUNT")
                    .value(transaction.getDestAcctNo())
                    .build());
            importParams.add(Item.builder()
                    .code("DEBIT_ACCOUNT")
                    .value(transaction.getSourceAcctNo())
                    .build());
            importParams.add(Item.builder()
                    .code("TRANS_AMOUNT")
                    .value(String.valueOf(transaction.getTransAmt()))
                    .build());
            importParams.add(Item.builder()
                    .code("BILLER_REFERENCE")
                    .value(String.valueOf(transaction.getMcpTransId()))
                    .build());
            importParams.add(Item.builder()
                    .code("TRANS_DESCRIPTION")
                    .value(transaction.getDescription())
                    .build());
            RequestInput dataWrapper = new RequestInput();
            dataWrapper.setInputItems(InputItems.builder()
                    .items(importParams)
                    .build());
            if (transaction.getTransType().equals("P2P")) {
                serviceRequestBody.setServiceCode("FUNDS_TRANSFER");
            } else if (transaction.getTransType().equals("GL2GL")) {
                serviceRequestBody.setServiceCode("GL_TO_GL");
            } else if (transaction.getTransType().equals("GL2DP")) {
                serviceRequestBody.setServiceCode("GL_TO_DEPOSIT");
            } else if (transaction.getTransType().equals("DP2GL")) {
                serviceRequestBody.setServiceCode("DEPOSIT_TO_GL");
            } else {
                return TxnResult.builder()
                        .code("-99")
                        .message("Invalid Transaction type code [" + transaction.getTransType() + "]").build();
            }
            serviceRequestBody.setRequestId(generateRequestId());
            serviceRequestBody.setRequestInput(dataWrapper);
            ServiceResponseWrapper response = processServiceRequest(ServiceRequestWrapper.builder()
                    .authRequest(getAuthorizeRequest())
                    .requestBody(serviceRequestBody)
                    .build());
            TxnResult txnResult = getRespMessage(response);
            if (!txnResult.getCode().equals("00")) {
                return txnResult;
            }
            TransRespData transResponse = getTranResp(response.getRequestOutput());
            return TxnResult.builder()
                    .code("00")
                    .message("Approved")
                    .data(transResponse)
                    .build();
        } catch (MediumException e1) {
            Logger.logError(e1.toString());
            return TxnResult.builder()
                    .code(e1.getErrorMessage().getCode())
                    .message(e1.getErrorMessage().getMessage()).build();
        } catch (Exception e) {
            Logger.logError(e.toString());
            return TxnResult.builder()
                    .code("-99")
                    .message("System error occurred while processing transaction at eQuiWeb").build();
        }
    }

    public TxnResult reverseTransaction(List<TransDetails> childTrans, String reversalReason) {
        TxnResult txnResult = null;
        try {
            for (int j = 0; j < childTrans.size(); j++) {
                childTrans.get(j).setDescription("*Reversal*-" + reversalReason);
                if (childTrans.get(j).getTransType().equals("GL2DP")) {
                    childTrans.get(j).setTransType("DP2GL");
                } else if (childTrans.get(j).getTransType().equals("DP2GL")) {
                    childTrans.get(j).setTransType("GL2DP");
                }
                txnResult = fundsTransfer(childTrans.get(j));
            }
            return txnResult;
        } catch (Exception e) {
            Logger.logError(e.toString());
            return TxnResult.builder()
                    .code("-99")
                    .message(e.getMessage() + " at eQuiWeb").build();
        }
    }

    public void validateSourceAccountBalance(TransRequestData request, List<TransDetails> transDetailsList) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (TransDetails transDetail : transDetailsList) {
            if (transDetail.getSourceAcctNo().trim().equals(request.getSourceAcctNo().trim()))
                totalAmount = totalAmount.add(transDetail.getTransAmt());
        }
        if (!request.getChannelCode().equals("MOBILE")) {
            return;
        }
        Logger.logError("====================================== Source Account number to check balance: " + request.getSourceAcctNo());
        if (request.getSourceAcctNo().length() > 12) {
            return;
        }
        Logger.logError("====================================== Validate  Source Account number length passed!!: " + request.getSourceAcctNo().length());
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setAcctNo(request.getSourceAcctNo());
        TxnResult accountBalanceResponse = doAccountBalance(accountRequest);
        if (!accountBalanceResponse.getCode().equals("00")) {
            throw new MediumException(ErrorData.builder()
                    .code("-99")
                    .message(accountBalanceResponse.getMessage() + " during balance check").build());
        }
        CIAccountBalance ciAccountBalance = (CIAccountBalance) accountBalanceResponse.getData();
        if (ciAccountBalance.getAvailableBalance().compareTo(totalAmount) < 0) {
            throw new MediumException(ErrorData.builder()
                    .code("-99")
                    .message("Account " + request.getSourceAcctNo() + " has insufficient funds").build());
        }
    }


    public TxnResult doAccountFullStatement(CIStatementRequest request) {
        ServiceRequestBody serviceRequestBody = new ServiceRequestBody();
        List<Item> importParams = new ArrayList<>();
        try {
            importParams.add(Item.builder()
                    .code("PRIMARY_ACCOUNT")
                    .value(request.getAccountNo())
                    .build());
            importParams.add(Item.builder()
                    .code("DEP_LOAN")
                    .value("DP")
                    .build());
            importParams.add(Item.builder()
                    .code("FROM_DATE")
                    .value(this.sdf.format(request.getFromDate()))
                    .build());
            importParams.add(Item.builder()
                    .code("TO_DATE")
                    .value(this.sdf.format(request.getToDate()))
                    .build());
            RequestInput dataWrapper = new RequestInput();
            dataWrapper.setInputItems(InputItems.builder()
                    .items(importParams)
                    .build());
            serviceRequestBody.setServiceCode("ACCOUNT_STATEMENT");
            serviceRequestBody.setRequestId(generateRequestId());
            serviceRequestBody.setRequestInput(dataWrapper);
            ServiceResponseWrapper response = processServiceRequest(ServiceRequestWrapper.builder()
                    .authRequest(getAuthorizeRequest())
                    .requestBody(serviceRequestBody)
                    .build());
            TxnResult txnResult = getRespMessage(response);
            if (!txnResult.getCode().equals("00"))
                return txnResult;
            return TxnResult.builder()
                    .code("00")
                    .message("Approved")
                    .data(getAccountStatementResp(response.getRequestOutput(), request.getStatementType()))
                    .build();
        } catch (Exception e) {
            Logger.logError(e.toString());
            return TxnResult.builder()
                    .code("-99")
                    .message("System error occurred while processing account statement at eQuiWeb").build();
        }
    }

    public TxnResult accountInquiryByPhoneNo(CIAccount request) {
        ServiceRequestBody serviceRequestBody = new ServiceRequestBody();
        List<Item> importParams = new ArrayList<>();
        try {
            importParams.add(Item.builder()
                    .code("MOBILE_PHONE")
                    .value(request.getPhoneNo())
                    .build());
            RequestInput dataWrapper = new RequestInput();
            dataWrapper.setInputItems(InputItems.builder()
                    .items(importParams)
                    .build());
            serviceRequestBody.setServiceCode("CUSTOMER_INQUIRY");
            serviceRequestBody.setRequestId(generateRequestId());
            serviceRequestBody.setRequestInput(dataWrapper);
            ServiceResponseWrapper response = processServiceRequest(ServiceRequestWrapper.builder()
                    .authRequest(getAuthorizeRequest())
                    .requestBody(serviceRequestBody)
                    .build());
            TxnResult txnResult = getRespMessage(response);
            if (!txnResult.getCode().equals("00")) {
                return txnResult;
            }
            CICustomer ciCustomer = getPhoneValidationResp(response.getRequestOutput());
            List<CIAccount> ciAccountList = getCustomerAccounts(response.getRequestOutput());
            ciCustomer.setAccountList(ciAccountList);
            return TxnResult.builder()
                    .code("00")
                    .message("Approved")
                    .data(ciCustomer)
                    .build();
        } catch (Exception e) {
            Logger.logError(e.toString());
            return TxnResult.builder()
                    .code("-99")
                    .message("System error occurred while processing mobile phone validation at eQuiWeb").build();
        }
    }


    @Transactional
    public TxnResult fundsTransfer(TransRequestData request, List<TransDetails> childTrans, boolean isReversalFlag) {
        boolean isMainTransReversed = false;
        TransRespData mainTxnResponse = null;
        TxnResult txnResult = null;

        // StringBuilder to accumulate logs
        StringBuilder logBuilder = new StringBuilder();
        String mainTxnId = null;
        try {
            // Ensure the transaction details are not null
            if (childTrans == null) {
                return TxnResult.builder()
                        .code("-99")
                        .message("Transaction details cannot be null").build();
            }

            // Extract the main transaction ID
            mainTxnId = String.valueOf(childTrans.get(0).getMcpTransId());

            // Validate the source account balance
            logBuilder.append("Main Transaction ID: ").append(mainTxnId).append(" - Validating source account balance\n");
            validateSourceAccountBalance(request, childTrans);

            // Iterate through child transactions
            for (int i = 0; i < childTrans.size(); i++) {
                txnResult = fundsTransfer(childTrans.get(i));

                if (txnResult.getCode().equals("00")) {
                    TransRespData non_main_txn = (TransRespData) txnResult.getData();
                    logBuilder.append("Main Transaction ID: ").append(mainTxnId)
                            .append(" - Successfully processed transaction with CBS Trans ID: ").append(non_main_txn.getCbsTransId()).append("\n");

                    childTrans.get(i).setCbsTransId(non_main_txn.getCbsTransId());

                    if (childTrans.get(i).isMain()) {
                        mainTxnResponse = non_main_txn;
                    }

                    if (mainTxnResponse != null) {
                        if (request.getSourceAcctNo().equalsIgnoreCase(childTrans.get(i).getSourceAcctNo()))
                            mainTxnResponse.setDrAcctBalance(non_main_txn.getDrAcctBalance());
                        if (request.getDestAcctNo().equalsIgnoreCase(childTrans.get(i).getDestAcctNo()))
                            mainTxnResponse.setCrAcctBalance(non_main_txn.getCrAcctBalance());
                    }

                    if (isReversalFlag && childTrans.get(i).getAmountType().equals("TRANS_AMOUNT")) {
                        isMainTransReversed = true;
                    }

                    String status = isReversalFlag ? "Reversed" : "Posted";
                    logBuilder.append("Main Transaction ID: ").append(mainTxnId)
                            .append(" - Updating status to: ").append(status).append("\n");
                    this.transactionDetailRepo.updateTxnDetailStatus(childTrans.get(i).getMcpTransDetailId(), "Y", txnResult.getMessage(), status, childTrans.get(i).getCbsTransId());

                    continue;
                }

                this.transactionDetailRepo.updateTxnDetailStatus(childTrans.get(i).getMcpTransDetailId(), "N", txnResult.getMessage(), "Failed", "0");

                try {
                    if (isReversalFlag) {
                        logBuilder.append("Main Transaction ID: ").append(mainTxnId).append(" - Marking as RevPending\n");
                        this.transactionDetailRepo.updateTxnDetailStatus(childTrans.get(i).getMcpTransDetailId(), "N", txnResult.getMessage(), "RevPending", "0");
                        continue;
                    }
                } catch (Exception e) {
                    logBuilder.append("Main Transaction ID: ").append(mainTxnId)
                            .append(" - Error during RevPending update: ").append(e.getMessage()).append("\n");
                }

                logBuilder.append("Main Transaction ID: ").append(mainTxnId)
                        .append(" - Entering Reversal Logic. Code: ").append(txnResult.getCode())
                        .append(" - ").append(txnResult.getMessage()).append("\n");

                if (!isReversalFlag) {
                    for (int j = 0; j < i; j++) {
                        if (childTrans.get(j).getCbsTransId() == null)
                            continue;

                        String destinationAccount = childTrans.get(j).getSourceAcctNo();
                        String sourceAccount = childTrans.get(j).getDestAcctNo();
                        childTrans.get(j).setDestAcctNo(destinationAccount);
                        childTrans.get(j).setSourceAcctNo(sourceAccount);
                        childTrans.get(j).setDescription("*Reversal*-" + childTrans.get(j).getDescription());

                        if (childTrans.get(j).getTransType().equals("GL2DP")) {
                            childTrans.get(j).setTransType("DP2GL");
                        } else if (childTrans.get(j).getTransType().equals("DP2GL")) {
                            childTrans.get(j).setTransType("GL2DP");
                        }

                        TxnResult result = fundsTransfer(childTrans.get(j));
                        if (result.getCode().equals("00")) {
                            mainTxnResponse.setReversed(isMainTransReversed);
                            logBuilder.append("Main Transaction ID: ").append(mainTxnId)
                                    .append(" - Reversal successful for child transaction with CBS Trans ID: ").append(childTrans.get(j).getCbsTransId()).append("\n");
                            this.transactionDetailRepo.updateTxnDetailStatus(childTrans.get(j).getMcpTransDetailId(), "Y", null, "Reversed", "0");
                        }
                    }
                }
                break;
            }

            if (isReversalFlag && isMainTransReversed) {
                logBuilder.append("Main Transaction ID: ").append(mainTxnId)
                        .append(" - Reversal of main transaction successful. Cancelling all pending transactions.\n");
                this.transactionDetailRepo.updateAllPendingTxnStatuses(childTrans.get(0).getMcpTransId(), "N", null, "Cancelled");
                return TxnResult.builder()
                        .code("00")
                        .message("Approved").build();
            }

            if (mainTxnResponse != null) {
                txnResult.setData(mainTxnResponse);
            }

            if (!txnResult.getCode().equals("00")) {
                logBuilder.append("Main Transaction ID: ").append(mainTxnId)
                        .append(" - Cancelling all pending transactions due to failure.\n");
                this.transactionDetailRepo.updateAllPendingTxnStatuses(childTrans.get(0).getMcpTransId(), "N", null, "Cancelled");
            }

            return txnResult;

        } catch (Exception e) {
            logBuilder.append("Main Transaction ID: ").append(mainTxnId).append(" - Exception occurred: ").append(e.getMessage()).append("\n");
            TransRespData data = new TransRespData();
            data.setReversed(false);
            this.transactionDetailRepo.updateAllPendingTxnStatuses(childTrans.get(0).getMcpTransId(), "N", null, "Cancelled");
            return TxnResult.builder()
                    .code("-99")
                    .data(data)
                    .message(e.getMessage() + " at eQuiWeb").build();
        } finally {
            // Log everything at once in the finally block
            Logger.logInfo(logBuilder.toString());
        }
    }


    public TxnResult doAccountBalance(AccountRequest request) {
        ServiceRequestBody serviceRequestBody = new ServiceRequestBody();
        List<Item> importParams = new ArrayList<>();
        try {
            importParams.add(Item.builder()
                    .code("PRIMARY_ACCOUNT")
                    .value(request.getAcctNo())
                    .build());
            RequestInput dataWrapper = new RequestInput();
            dataWrapper.setInputItems(InputItems.builder()
                    .items(importParams)
                    .build());
            serviceRequestBody.setServiceCode("BALANCE_INQUIRY");
            serviceRequestBody.setRequestId(generateRequestId());
            serviceRequestBody.setRequestInput(dataWrapper);
            ServiceResponseWrapper response = processServiceRequest(ServiceRequestWrapper.builder()
                    .authRequest(getAuthorizeRequest())
                    .requestBody(serviceRequestBody)
                    .build());
            TxnResult txnResult = getRespMessage(response);
            if (!txnResult.getCode().equals("00")) {
                return txnResult;
            }
            CIAccountBalance accountBalance = getAccountBalanceResp(response.getRequestOutput());
            return TxnResult.builder()
                    .code("00")
                    .message("Approved")
                    .data(accountBalance)
                    .build();
        } catch (Exception e) {
            Logger.logError(e.toString());
            return TxnResult.builder()
                    .code("-99")
                    .message("System error occurred while processing account balance at eQuiWeb").build();
        }
    }

    public TxnResult accountInquiryByAccountNo(CIAccount request) {
        ServiceRequestBody serviceRequestBody = new ServiceRequestBody();
        List<Item> importParams = new ArrayList<>();
        try {
            importParams.add(Item.builder()
                    .code("PRIMARY_ACCOUNT")
                    .value(request.getAccountNo())
                    .build());
            RequestInput dataWrapper = new RequestInput();
            dataWrapper.setInputItems(InputItems.builder()
                    .items(importParams)
                    .build());
            serviceRequestBody.setServiceCode("CUSTOMER_INQUIRY");
            serviceRequestBody.setRequestId(generateRequestId());
            serviceRequestBody.setRequestInput(dataWrapper);
            ServiceResponseWrapper response = processServiceRequest(ServiceRequestWrapper.builder()
                    .authRequest(getAuthorizeRequest())
                    .requestBody(serviceRequestBody)
                    .build());
            TxnResult txnResult = getRespMessage(response);
            if (!txnResult.getCode().equals("00")) {
                return txnResult;
            }
            CIAccount accountBalance = getAccountValidationResp(request.getAccountNo(), response.getRequestOutput());
            return TxnResult.builder()
                    .code("00")
                    .message("Approved")
                    .data(accountBalance)
                    .build();
        } catch (Exception e) {
            Logger.logError(e.toString());
            return TxnResult.builder()
                    .code("-99")
                    .message("System error occurred while processing account validation at eQuiWeb").build();
        }
    }

    private CIAccount getCustomerAccount(RequestOutput requestOutput) {
        CIAccount resp = new CIAccount();
        if (requestOutput.getOutputParameters() == null) {
            return resp;
        }
        for (ExportItem item : requestOutput.getOutputParameters().getExportItems()) {
            if (item.getCode().equalsIgnoreCase("RIM_NO")) {
                resp.setCustomerNo(item.getValue());
                continue;
            }
            if (item.getCode().equalsIgnoreCase("ACCOUNT_NUMBER")) {
                resp.setAccountNo(item.getValue());
            }
        }
        return resp;
    }

    public TxnResult createCustomer(CustomerDetail request) {
        ServiceRequestBody serviceRequestBody = new ServiceRequestBody();
        List<Item> importParams = new ArrayList<>();
        try {
            importParams.add(Item.builder()
                    .code("FIRST_NAME")
                    .value(request.getFirstName())
                    .build());
            importParams.add(Item.builder()
                    .code("LAST_NAME")
                    .value(request.getSurName())
                    .build());
            importParams.add(Item.builder()
                    .code("BIRTH_DT")
                    .value(DataUtils.toString(request.getDateOfBirth()))
                    .build());
            importParams.add(Item.builder()
                    .code("TOWN_ID")
                    .value(request.getTown())
                    .build());
            importParams.add(Item.builder()
                    .code("IDENT_ID")
                    .value(String.valueOf(request.getIdType()))
                    .build());
            importParams.add(Item.builder()
                    .code("ID_VALUE")
                    .value(request.getIdNumber())
                    .build());
            importParams.add(Item.builder()
                    .code("TITLE_ID")
                    .value(String.valueOf(request.getTitleId()))
                    .build());
            importParams.add(Item.builder()
                    .code("MOBILE_PHONE")
                    .value(request.getMobilePhone())
                    .build());
            importParams.add(Item.builder()
                    .code("ID_EXPIRY_DT")
                    .value(DataUtils.toString(request.getIdExpiryDate()))
                    .build());
            importParams.add(Item.builder()
                    .code("ID_ISSUE_DT")
                    .value(DataUtils.toString(request.getDateOfBirth()))
                    .build());
            importParams.add(Item.builder()
                    .code("GENDER")
                    .value(request.getGender())
                    .build());
            importParams.add(Item.builder()
                    .code("CLASS_CODE")
                    .value("1")
                    .build());
            importParams.add(Item.builder()
                    .code("CUSTOMER_PHOTO")
                    .value(request.getPhotoBase64String())
                    .build());
            importParams.add(Item.builder()
                    .code("CUSTOMER_SIGNATURE")
                    .value(request.getSignatureBase64String())
                    .build());
            RequestInput dataWrapper = new RequestInput();
            dataWrapper.setInputItems(InputItems.builder()
                    .items(importParams)
                    .build());
            serviceRequestBody.setServiceCode("CREATE_CUSTOMER_DATA");
            serviceRequestBody.setRequestId(generateRequestId());
            serviceRequestBody.setRequestInput(dataWrapper);
            ServiceResponseWrapper response = processServiceRequest(ServiceRequestWrapper.builder()
                    .authRequest(getAuthorizeRequest())
                    .requestBody(serviceRequestBody)
                    .build());
            TxnResult txnResult = getRespMessage(response);
            if (!txnResult.getCode().equals("00")) {
                return txnResult;
            }
            CIAccount transResponse = getCustomerAccount(response.getRequestOutput());
            return TxnResult.builder()
                    .code("00")
                    .message("Approved")
                    .data(transResponse)
                    .build();
        } catch (Exception e) {
            Logger.logError(e.toString());
            return TxnResult.builder()
                    .code("-99")
                    .message("System error occurred while processing customer registration at eQuiWeb").build();
        }
    }


}
