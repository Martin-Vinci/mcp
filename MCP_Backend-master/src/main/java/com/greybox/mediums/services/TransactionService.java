package com.greybox.mediums.services;

import com.greybox.mediums.entities.*;
import com.greybox.mediums.models.*;
import com.greybox.mediums.models.equiweb.CIAccountBalance;
import com.greybox.mediums.models.equiweb.CIStatementRequest;
import com.greybox.mediums.repository.*;
import com.greybox.mediums.utils.*;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    @Autowired
    private TransactionRefRepo transactionRefRepo;
    @Autowired
    private TransactionDetailRepo transactionDetailRepo;
    @Autowired
    private ProductService productService;
    @Autowired
    private ServicePostingDetailRepo servicePostingDetailRepo;
    @Autowired
    private MobileUserAccountRepo mobileUserAccountRepo;
    @Autowired
    private SystemParameterService systemParameterRepo;
    @Autowired
    private ProductChargeService productChargeService;
    @Autowired
    private ProductCommissionService productCommissionService;
    @Autowired
    private ReportRepo reportRepo;
    @Autowired
    private EquiWebService equiWebService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private TransCenteTrustRepo transCenteTrustRepo;
    @Autowired
    private TransCenteTrustSummaryRepo transCenteTrustSummaryRepo;

    public TxnResult findTransactions(TransactionRef request) {
        List<TransactionRef> customers = this.reportRepo.findTransactions(request);
        return customers == null ? TxnResult.builder().code("404").message("No records found").build() : TxnResult.builder().message("approved").code("00").data(customers).build();
    }

    public TxnResult findTransCenteTrustSummary(TransactionRef request) {
        List<TransCenteTrustSummary> customers = this.transCenteTrustSummaryRepo.findTransCenteTrust(request.getStartDate(), request.getEndDate());
        return customers == null ? TxnResult.builder().code("404").message("No records found").build() : TxnResult.builder().message("approved").code("00").data(customers).build();
    }

    public TxnResult findTransCenteTrustDetails(TransactionRef request) {
        List customers;
        if (request.getServiceCode() != null) {
            customers = this.transCenteTrustRepo.findTransCenteTrust(request.getId(), request.getServiceCode());
        } else {
            customers = this.transCenteTrustRepo.findTransCenteTrust(request.getId());
        }

        return customers == null ? TxnResult.builder().code("404").message("No records found").build() : TxnResult.builder().message("approved").code("00").data(customers).build();
    }

    public TxnResult findTransCenteTrust(TransCenteTrust request) {
        List<TransCenteTrust> customers = this.transCenteTrustRepo.findTransCenteTrust(request.getMainTransId());
        return customers == null ? TxnResult.builder().code("404").message("No records found").build() : TxnResult.builder().message("approved").code("00").data(customers).build();
    }

    public TxnResult findTransactionDetails(Long transId) {
        List<TransactionDetail> customers = this.transactionDetailRepo.findTransactionDetailReport(transId);
        return customers == null ? TxnResult.builder().code("404").message("No records found").build() : TxnResult.builder().message("approved").code("00").data(customers).build();
    }

    private void validateTransactionParameters(ServiceRef serviceRef, TransRequestData request) {
        if (serviceRef.getTransType().trim().equals("BILL")) {
            if (serviceRef.getDebitCredit().trim().equals("DEBIT")) {
                request.setSourceAcctNo(serviceRef.getBillerAcctNo());
            } else {
                request.setDestAcctNo(serviceRef.getBillerAcctNo());
            }
        }

        if (request.getSourceAcctNo() == null) {
            throw new MediumException(ErrorData.builder().code("-99").message("Source account number cannot be empty").build());
        } else if (request.getDestAcctNo() == null) {
            throw new MediumException(ErrorData.builder().code("-99").message("Destination account number cannot be empty").build());
        } else if (request.getDestAcctNo().trim().equals(request.getSourceAcctNo().trim())) {
            throw new MediumException(ErrorData.builder().code("-99").message("Source and Destination account number cannot be the same").build());
        }
    }

    private List<TransDetails> storeAndGenerateCbsTransDetails(List<TransactionDetail> child, boolean pendChildTxns) {
        List<TransDetails> transDetailsList = new ArrayList<>();
        for (int i = 0; i < child.size(); i++) {
            transactionDetailRepo.save(child.get(i));
            if (pendChildTxns) {
                if (List.of("TRANS_AMOUNT", "CHARGE", "EXCISE_DUTY").contains(child.get(i).getTransType()))
                    transDetailsList.add(TransDetails.builder().description(child.get(i).getTransDescr()).destAcctNo(child.get(i).getCrAcctNo()).mcpTransId(child.get(i).getMainTransId()).sourceAcctNo(child.get(i).getDrAcctNo()).transAmt(child.get(i).getAmount()).transType(child.get(i).getEntryType()).amountType(child.get(i).getTransType()).mcpTransDetailId(child.get(i).getId()).isMain(child.get(i).getItemNo() == 1).build());
            } else {
                transDetailsList.add(TransDetails.builder().description(child.get(i).getTransDescr()).destAcctNo(child.get(i).getCrAcctNo()).mcpTransId(child.get(i).getMainTransId()).sourceAcctNo(child.get(i).getDrAcctNo()).transAmt(child.get(i).getAmount()).transType(child.get(i).getEntryType()).amountType(child.get(i).getTransType()).mcpTransDetailId(child.get(i).getId()).isMain(child.get(i).getItemNo() == 1).build());
            }
        }
        return transDetailsList;
    }

    @Transactional
    public TxnResult fundsTransfer(TransRequestData request) throws MediumException {

//        TxnResult resp = logTransaction(request);
//        TransRespData respData = (TransRespData) resp.getData();
//        request.setOriginTransId(respData.getTransId());
//        resp = completeTransaction(request);
//        if (1 == 1)
//            return resp;


        // Validate service configuration and transaction parameters
        ServiceRef serviceRef = productService.validateServiceConfiguration(request);
        validateTransactionParameters(serviceRef, request);

        // Prepare transaction description
        String description = serviceRef.getTransLiteral() == null ? request.getDescription() : serviceRef.getTransLiteral().trim() + "-" + request.getDescription();

        // Create and save new transaction record
        TransactionRef transaction = new TransactionRef();
        transaction.setCrAcctNo(request.getDestAcctNo());
        transaction.setDrAcctNo(request.getSourceAcctNo());
        transaction.setAmount(request.getTransAmt());
        transaction.setIsoCode(request.getCurrency());
        transaction.setPostedBy(request.getOutletCode());
        transaction.setServiceCode(request.getServiceCode());
        transaction.setTransDescr(description);
        transaction.setSuccessFlag("Y");
        transaction.setTransDate(DataUtils.getCurrentTimeStamp().toLocalDateTime());
        transaction.setUtilPosted("Y");
        transaction.setReversalFlag("N");
        transaction.setExternalTransRef(request.getExternalReference());
        transaction.setDepositorPhone(request.getDepositorPhoneNo());
        transaction.setDepositorName(request.getDepositorName());
        transaction.setSystemDate(DataUtils.getCurrentTimeStamp().toLocalDateTime());
        transaction.setInitiatorPhoneNo(request.getUserPhoneNo());
        transaction.setTransactionMode("COMPLETED");
        TransactionRef transactionRef = transactionRefRepo.save(transaction);

        // Fetch service posting policies
        List<ServicePostingDetail> postingPolicies = servicePostingDetailRepo.findServicePostingPolicies(serviceRef.getServiceId());
        if (postingPolicies.isEmpty()) {
            throw new MediumException(ErrorData.builder().code("404").message("No Transaction Policy exists for the transaction type").build());
        }

        // Process entity accounts and amounts
        EntityAccounts entityAccounts = getEntityAccounts(request, serviceRef);
        EntityAmount entityAmount = getEntityAmount(request, serviceRef, postingPolicies);

        // Process child transactions and prepare CBS object
        List<TransactionDetail> childTransactions = processChildTransactions(request, transactionRef, postingPolicies, entityAccounts, entityAmount);
        prepareDTBExciseShare(transaction, childTransactions, entityAccounts);

        List<TransDetails> transDetailsList = storeAndGenerateCbsTransDetails(childTransactions, true);

        // Perform the funds transfer
        TxnResult transactionResponse = equiWebService.fundsTransfer(request, transDetailsList, false);

        // Handle transaction failure and update status
        if (!"00".equals(transactionResponse.getCode())) {
            transactionRefRepo.updateTransactionStatus(transactionRef.getId(), "N", "N", "Y", transactionResponse.getMessage(), "COMPLETED");
            return TxnResult.builder().message(transactionResponse.getMessage()).code(transactionResponse.getCode()).build();
        }

        // Process transaction reversal data
        TransReversalData reversalData = new TransReversalData();
        reversalData.setOriginTranId(transactionRef.getId());

        // Update account balances for credit and debit accounts based on CBS response
        TransRespData cbsResponseData = (TransRespData) transactionResponse.getData();
        updateAccountBalance(transactionRef.getCrAcctNo(), cbsResponseData.getCrAcctBalance());
        updateAccountBalance(transactionRef.getDrAcctNo(), cbsResponseData.getDrAcctBalance());

        // Set response data and charge amount
        TransRespData transRespData = prepareTransRespData(transactionRef, childTransactions, cbsResponseData, request);

        // Update transaction reference with CBS transaction ID
        transactionRef.setExternalTransRef(cbsResponseData.getCbsTransId());
        transactionRef = prepareCommissionData(transactionRef, childTransactions);
        transactionRefRepo.save(transactionRef);

        // Return final transaction result
        return TxnResult.builder().message("approved").code("00").data(transRespData).build();
    }

    // Helper method to update account balance and last activity date
    private void updateAccountBalance(String acctNo, BigDecimal balance) {
        if (acctNo != null) {
            mobileUserAccountRepo.updateAccountLastActivityDate(acctNo);
            mobileUserAccountRepo.updateAccountBalance(acctNo, balance);
        }
    }

    // Helper method to prepare transaction response data
    private TransRespData prepareTransRespData(TransactionRef transactionRef, List<TransactionDetail> child, TransRespData cbsResponseData, TransRequestData request) {
        TransRespData transRespData = new TransRespData();
        transRespData.setCbsTransId(cbsResponseData.getCbsTransId());

        try {
            transRespData.setDrAcctBalance(cbsResponseData.getDrAcctBalance());
        } catch (Exception var7) {
            transRespData.setDrAcctBalance(BigDecimal.ZERO);
        }

        try {
            transRespData.setCrAcctBalance(cbsResponseData.getCrAcctBalance());
        } catch (Exception var6) {
            transRespData.setCrAcctBalance(BigDecimal.ZERO);
        }
        transRespData.setChargeAmt(getChargeAmount(request, child));
        transRespData.setTransId(transactionRef.getId());
        List<TransDetails> transDetailsList = new ArrayList<>();
        for (int i = 0; i < child.size(); i++) {
            transDetailsList.add(TransDetails.builder().description(child.get(i).getTransDescr()).destAcctNo(child.get(i).getCrAcctNo()).mcpTransId(child.get(i).getMainTransId()).sourceAcctNo(child.get(i).getDrAcctNo()).transAmt(child.get(i).getAmount()).transType(child.get(i).getEntryType()).amountType(child.get(i).getTransType()).mcpTransDetailId(child.get(i).getId()).isMain(child.get(i).getItemNo() == 1).build());
        }
        transRespData.setTransDetails(transDetailsList);
        // Handle service-specific charge overrides
        if (request.getServiceCode() == 70043 || request.getServiceCode() == 70040) {
            transRespData.setChargeAmt(BigDecimal.ZERO);
        }
        return transRespData;
    }


    @Transactional
    public TxnResult logTransaction(TransRequestData request) throws MediumException {
        // Validate service configuration for the transaction
        ServiceRef serviceRef = productService.validateServiceConfiguration(request);

        // Validate the necessary transaction parameters
        validateTransactionParameters(serviceRef, request);

        // Determine transaction description
        String description = (serviceRef.getTransLiteral() == null) ? request.getDescription() : serviceRef.getTransLiteral().trim() + "-" + request.getDescription();

        // Create new transaction reference object
        TransactionRef transaction = new TransactionRef();
        transaction.setCrAcctNo(request.getDestAcctNo());
        transaction.setDrAcctNo(request.getSourceAcctNo());
        transaction.setAmount(request.getTransAmt());
        transaction.setIsoCode(request.getCurrency());
        transaction.setPostedBy(request.getOutletCode());
        transaction.setServiceCode(request.getServiceCode());
        transaction.setTransDescr(description);
        transaction.setSuccessFlag("N");
        transaction.setTransDate(DataUtils.getCurrentTimeStamp().toLocalDateTime());
        transaction.setUtilPosted("N");
        transaction.setReversalFlag("N");
        transaction.setExternalTransRef(request.getExternalReference());
        transaction.setDepositorPhone(request.getDepositorPhoneNo());
        transaction.setDepositorName(request.getDepositorName());
        transaction.setSystemDate(DataUtils.getCurrentTimeStamp().toLocalDateTime());
        transaction.setInitiatorPhoneNo(request.getUserPhoneNo());
        transaction.setTransactionMode("PENDING");

        // Save the transaction reference
        TransactionRef savedTransaction = transactionRefRepo.save(transaction);

        // Retrieve transaction posting policies for the service
        List<ServicePostingDetail> postingPolicies = servicePostingDetailRepo.findServicePostingPolicies(serviceRef.getServiceId());
        if (postingPolicies.isEmpty()) {
            throw new MediumException(ErrorData.builder().code("404").message("No Transaction Policy exists for the transaction type").build());
        }

        // Get entity accounts and amount for the transaction
        EntityAccounts entityAccounts = getEntityAccounts(request, serviceRef);
        EntityAmount entityAmount = getEntityAmount(request, serviceRef, postingPolicies);

        // Process child transactions and save transaction details
        List<TransactionDetail> childTransactions = processChildTransactions(request, savedTransaction, postingPolicies, entityAccounts, entityAmount);
        prepareDTBExciseShare(transaction, childTransactions, entityAccounts);


        List<TransDetails> transDetailsList = storeAndGenerateCbsTransDetails(childTransactions, true);
        // Prepare the transaction response data
        TransRespData transRespData = new TransRespData();
        transRespData.setTransId(savedTransaction.getId());
        transRespData.setTransDetails(transDetailsList);

        // Prepare commission data for the transaction
        TransactionRef updatedTransaction = prepareCommissionData(savedTransaction, childTransactions);
        transactionRefRepo.save(updatedTransaction);

        // Set charge amount, conditionally handle exceptions
        BigDecimal chargeAmount = getChargeAmount(request, childTransactions);
        if (request.getServiceCode() == 70043 || request.getServiceCode() == 70040) {
            transRespData.setChargeAmt(BigDecimal.ZERO);
        } else {
            transRespData.setChargeAmt(chargeAmount);
        }

        // Return the transaction result
        return TxnResult.builder().message("approved").code("00").data(transRespData).build();
    }


    @Transactional
    public TxnResult completeTransaction(TransRequestData request) {
        List<TransDetails> transDetailsList = new ArrayList<>();

        // Find the transaction details using the origin transaction ID
        TransactionRef transaction = transactionRefRepo.findTransactionRef(request.getOriginTransId());
        if (transaction == null) {
            throw new MediumException(ErrorData.builder().code("-99").message("Could not find transaction details for the specified transaction ID").build());
        }

        // Check if the transaction has already been posted
        if ("Y".equals(transaction.getSuccessFlag())) {
            throw new MediumException(ErrorData.builder().code("-99").message("Transaction is already posted").build());
        }

        // Fetch pending child transaction details
        Long txnId = transaction.getId();
        List<TransactionDetail> pendingTransactions = transactionDetailRepo.findPendingTransactionDetails(txnId);
        if (pendingTransactions == null || pendingTransactions.isEmpty()) {
            throw new MediumException(ErrorData.builder().code("-99").message("Could not find child items for the specified transaction ID").build());
        }

        // Update the request with source and destination account details
        request.setSourceAcctNo(transaction.getCrAcctNo().trim());
        request.setDestAcctNo(transaction.getDrAcctNo().trim());
        request.setChannelCode("SUPER_ADMIN");

        // Build the list of transaction details
        for (TransactionDetail detail : pendingTransactions) {
            transDetailsList.add(TransDetails.builder().transAmt(detail.getAmount()).sourceAcctNo(detail.getDrAcctNo()).destAcctNo(detail.getCrAcctNo()).amountType(detail.getTransType()).description(detail.getTransDescr().trim()).mcpTransId(request.getOriginTransId()).mcpTransDetailId(detail.getId()).isMain(detail.getItemNo() == 1).transType(StringUtil.getTransferType(detail.getDrAcctNo(), detail.getCrAcctNo())).build());
        }

        // Perform the funds transfer
        TxnResult txnResult = equiWebService.fundsTransfer(request, transDetailsList, false);

        // Handle failure case by saving the reversal reason
        if (!"00".equals(txnResult.getCode())) {
            transaction.setReversalReason(txnResult.getMessage());
            transactionRefRepo.save(transaction);
            return TxnResult.builder().message(txnResult.getMessage()).code(txnResult.getCode()).build();
        }

        // Update the transaction status after successful transfer
        transactionRefRepo.updateTransactionStatus(request.getOriginTransId(), "Y", "Y", "N", null, "COMPLETED");

        TransRespData cbsResponseData = (TransRespData) txnResult.getData();
        // Build the transaction response data
        TransRespData transRespData = prepareTransRespData(transaction, pendingTransactions, cbsResponseData, request);

        List<TransactionDetail> agentTransactions = pendingTransactions.stream()
                .filter(t -> "AGENT_COMMISSION".equals(t.getTransType()) || "TRANS_AMOUNT_AGENT_SHARE".equals(t.getTransType()))
                .collect(Collectors.toList());

        if (!agentTransactions.isEmpty()) {
            TransactionDetail commissionDetail = agentTransactions.get(0);
            EntityAmount entityAmount = new EntityAmount();
            entityAmount.setAgentCommissionAmount(commissionDetail.getAmount());
            String commissionAccount = commissionDetail.getCrAcctNo();

            // Asynchronously send a commission alert
            Executors.newSingleThreadExecutor().execute(() -> sendCommissionAlertMessage(request.getUserPhoneNo(), entityAmount.getAgentCommissionAmount(), transaction.getTransDescr().trim(), commissionAccount));
        }

        // Return the final transaction result
        return TxnResult.builder().message("approved").data(transRespData).code("00").build();
    }


    private static BigDecimal getChargeAmount(TransRequestData request, List<TransactionDetail> transDetailsList) {
        BigDecimal chargeAmount = BigDecimal.ZERO;

        for (int i = 0; i < transDetailsList.size(); ++i) {
            if (transDetailsList.get(i).getTransType().equalsIgnoreCase("CHARGE") && transDetailsList.get(i).getDrAcctNo().equals(request.getSourceAcctNo())) {
                chargeAmount = chargeAmount.add(transDetailsList.get(i).getAmount());
            } else if (transDetailsList.get(i).getTransType().equalsIgnoreCase("EXCISE_DUTY") && transDetailsList.get(i).getDrAcctNo().equals(request.getSourceAcctNo())) {
                chargeAmount = chargeAmount.add(transDetailsList.get(i).getAmount());
            } else if (transDetailsList.get(i).getTransType().equalsIgnoreCase("MOBILE_MONEY_TAX") && transDetailsList.get(i).getDrAcctNo().equals(request.getSourceAcctNo())) {
                chargeAmount = chargeAmount.add(transDetailsList.get(i).getAmount());
            }
        }

        return chargeAmount;
    }

//    @Transactional
//    public TxnResult validateTransaction(TransRequestData request) throws MediumException {
//        ServiceRef serviceRef = this.productService.validateServiceConfiguration(request);
//        this.validateTransactionParameters(serviceRef, request);
//        List<ServicePostingDetail> postingPolicies = this.servicePostingDetailRepo.findServicePostingPolicies(serviceRef.getServiceId());
//        if (postingPolicies.size() <= 0) {
//            throw new MediumException(ErrorData.builder().code("404").message("No Transaction Policy exists for the transaction type").build());
//        } else {
//            EntityAccounts entityAccounts = this.getEntityAccounts(request, serviceRef);
//            EntityAmount entityAmount = this.getEntityAmount(request, serviceRef, postingPolicies);
//            List<TransDetails> transDetailsList = this.validateChildTransactions(request, postingPolicies, entityAccounts, entityAmount);
//            BigDecimal chargeAmount = getChargeAmount(request, transDetailsList);
//            AccountRequest accountRequest = new AccountRequest();
//            accountRequest.setAcctNo(request.getSourceAcctNo());
//            TxnResult accountBalanceResponse = this.equiWebService.doAccountBalance(accountRequest);
//            if (!accountBalanceResponse.getCode().equals("00")) {
//                throw new MediumException(ErrorData.builder().code(accountBalanceResponse.getCode()).message(accountBalanceResponse.getMessage()).build());
//            } else {
//                CIAccountBalance ciAccountBalance = (CIAccountBalance) accountBalanceResponse.getData();
//                BigDecimal totalAmount = request.getTransAmt().add(chargeAmount);
//                if (ciAccountBalance.getAvailableBalance().compareTo(totalAmount) <= 0) {
//                    throw new MediumException(ErrorData.builder().code("404").message("Insufficient funds for this transaction").build());
//                } else {
//                    return TxnResult.builder().message("approved").code("00").build();
//                }
//            }
//        }
//    }

    private List<TransDetails> validateChildTransactions(TransRequestData request, List<ServicePostingDetail> postingPolicies, EntityAccounts entityAccounts, EntityAmount entityAmount) throws MediumException {
        List<TransDetails> transItems = new ArrayList();
        SystemParameter trustGLAccount = this.systemParameterRepo.getParameter("S17");
        if (trustGLAccount != null && trustGLAccount.getParamValue() != null) {
            SystemParameter micropayGLAccount = this.systemParameterRepo.getParameter("S28");
            if (micropayGLAccount != null && micropayGLAccount.getParamValue() != null) {
                SystemParameter trustAccountParam = this.systemParameterRepo.getParameter("S26");
                if (trustAccountParam != null && trustAccountParam.getParamValue() != null) {
                    SystemParameter micropayOperationAccount = this.systemParameterRepo.getParameter("S27");
                    if (micropayOperationAccount != null && micropayOperationAccount.getParamValue() != null) {
                        Iterator var11 = postingPolicies.iterator();

                        while (var11.hasNext()) {
                            ServicePostingDetail policy = (ServicePostingDetail) var11.next();
                            TransactionDetail child = this.generateChildTransactions(request, policy, entityAccounts, entityAmount);
                            child.setItemNo(policy.getPostingPriority());
                            PrintStream var10000 = System.out;
                            BigDecimal var10001 = child.getAmount();
                            var10000.println("================= Transaction Amount: " + var10001 + " Amount Type: " + policy.getAmountType());
                            if (child.getAmount() == null || child.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                                throw new MediumException(ErrorData.builder().code("-99").message("Amount [" + policy.getAmountType() + "] should be greater than 0").build());
                            }

                            if (child.getCrAcctNo() == null) {
                                throw new MediumException(ErrorData.builder().code("-99").message(policy.getDestinationAccount() + " [Account cannot be empty]").build());
                            }

                            if (child.getDrAcctNo() == null) {
                                throw new MediumException(ErrorData.builder().code("-99").message(policy.getSourceAccount() + " [Account cannot be empty]").build());
                            }

                            transItems.add(TransDetails.builder().description(child.getTransDescr()).destAcctNo(child.getCrAcctNo()).sourceAcctNo(child.getDrAcctNo()).transAmt(child.getAmount()).transType(policy.getTransCategory()).amountType(policy.getAmountType()).isMain(policy.getPostingPriority() == 1).build());
                        }

                        return transItems;
                    } else {
                        throw new MediumException(ErrorData.builder().code("404").message("Trust Account is not configured. Please contact system admin").build());
                    }
                } else {
                    throw new MediumException(ErrorData.builder().code("404").message("Trust Account is not configured. Please contact system admin").build());
                }
            } else {
                throw new MediumException(ErrorData.builder().code("404").message("Micropay GL Account is not configured. Please contact system admin").build());
            }
        } else {
            throw new MediumException(ErrorData.builder().code("404").message("Trust GL Account is not configured. Please contact system admin").build());
        }
    }

    private TransactionRef prepareCommissionData(TransactionRef transactionRef, List<TransactionDetail> transDetailsList) {
        if (transDetailsList != null) for (TransactionDetail transDetails : transDetailsList) {
            if (transDetails.getTransType().equals("NET_CHARGE"))
                transactionRef.setTotalCharge(transDetails.getAmount());
            if (transDetails.getTransType().equals("AGENT_COMMISSION"))
                transactionRef.setAgentCommission(transDetails.getAmount());
            if (transDetails.getTransType().equals("EXCISE_DUTY"))
                transactionRef.setExciseDuty(transDetails.getAmount());
            if (transDetails.getTransType().equals("WITHHOLD_TAX"))
                transactionRef.setWithholdTax(transDetails.getAmount());
            if (transDetails.getTransType().equals("TRANS_AMOUNT_BANK_SHARE"))
                transactionRef.setTotalCharge(transDetails.getAmount());
            if (transDetails.getTransType().equals("TRANS_AMOUNT_AGENT_SHARE"))
                transactionRef.setAgentCommission(transDetails.getAmount());
        }
        return transactionRef;
    }

    private List<TransactionDetail> processChildTransactions(TransRequestData request, TransactionRef baseTransaction, List<ServicePostingDetail> postingPolicies, EntityAccounts entityAccounts, EntityAmount entityAmount) throws MediumException {
        List<TransactionDetail> transItems = new ArrayList<>();

        SystemParameter trustGLAccount = systemParameterRepo.getParameter("S17");
        SystemParameter micropayGLAccount = systemParameterRepo.getParameter("S28");
        SystemParameter trustAccountParam = systemParameterRepo.getParameter("S26");
        SystemParameter micropayOperationAccount = systemParameterRepo.getParameter("S27");

        if (trustGLAccount == null || trustGLAccount.getParamValue() == null) {
            throw new MediumException(ErrorData.builder().code("404").message("Trust GL Account (S17) is not configured. Please contact system admin").build());
        }
        if (micropayGLAccount == null || micropayGLAccount.getParamValue() == null) {
            throw new MediumException(ErrorData.builder().code("404").message("Micropay GL Account (S28) is not configured. Please contact system admin").build());
        }
        if (trustAccountParam == null || trustAccountParam.getParamValue() == null) {
            throw new MediumException(ErrorData.builder().code("404").message("Trust Account (S26) is not configured. Please contact system admin").build());
        }
        if (micropayOperationAccount == null || micropayOperationAccount.getParamValue() == null) {
            throw new MediumException(ErrorData.builder().code("404").message("Micropay Operation Account (S27) is not configured. Please contact system admin").build());
        }

        for (ServicePostingDetail policy : postingPolicies) {
            TransactionDetail child = generateChildTransactions(request, policy, entityAccounts, entityAmount);
            child.setItemNo(policy.getPostingPriority());
            child.setMainTransId(baseTransaction.getId());
            System.out.println("================= Transaction Amount: " + child.getAmount() + " Amount Type: " + policy.getAmountType());

            if (child.getAmount() == null || child.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                // throw new MediumException(ErrorData.builder().code("-99").message("Amount [" + policy.getAmountType() + "] should be greater than 0").build());
                continue;
            }

            if (child.getCrAcctNo() == null || child.getDrAcctNo() == null) {
                throw new MediumException(ErrorData.builder().code("-99").message("Account cannot be empty").build());
            }

            child.setStatus("Pending");
            transItems.add(child);

            logCenteTrustTrans(child, policy, trustAccountParam.getParamValue(), micropayOperationAccount.getParamValue());
            if (policy.getTrustPostingCategory() != null) {
                String description, debitAcct, creditAcct, transType;
                if (policy.getTrustPostingCategory().trim().equals("TRUST_TO_MICROPAY")) {
                    description = "TRUST2MCP_" + child.getTransDescr();
                    transType = "TRUST2MCP_" + policy.getAmountType();
                    debitAcct = micropayGLAccount.getParamValue();
                    creditAcct = trustGLAccount.getParamValue();
                } else if (policy.getTrustPostingCategory().trim().equals("MICROPAY_TO_TRUST")) {
                    description = "MCP2TRUST~" + child.getTransDescr();
                    transType = "MCP2TRUST~" + policy.getAmountType();
                    debitAcct = trustGLAccount.getParamValue();
                    creditAcct = micropayGLAccount.getParamValue();
                } else {
                    continue;
                }
                transItems.add(TransactionDetail.builder().mainTransId(baseTransaction.getId()).crAcctNo(creditAcct).drAcctNo(debitAcct).amount(child.getAmount()).isoCode("UGX").status("Pending").transType(transType).entryType(policy.getTrustPostingCategory()).transDescr(description).postingDt(DataUtils.getCurrentTimeStamp()).itemNo(policy.getPostingPriority() * -1).createDate(DataUtils.getCurrentTimeStamp()).createdBy(baseTransaction.getPostedBy()).build());
            }
        }
        return transItems;
    }


    private void prepareDTBExciseShare(TransactionRef request, List<TransactionDetail> transactionDetailList, EntityAccounts entityAccounts) throws MediumException {

        if (request.getServiceCode() != 70024 && request.getServiceCode() != 70025) {
            return;
        }
        TransactionDetail exciseDutyTransaction = null;
        for (TransactionDetail detail : transactionDetailList) {
            if ("EXCISE_DUTY".equals(detail.getTransType())) {
                exciseDutyTransaction = detail;
                break; // Stop once the first match is found
            }
        }
        if (exciseDutyTransaction == null) return;

        // Get the max value
        Integer maxItemNo = transactionDetailList.stream().map(TransactionDetail::getItemNo)    // Extract the itemNo field
                .max(Comparator.naturalOrder())              // Convert Integer to long
                .orElse(0);                          // Provide a default value if the list is empty

        maxItemNo = maxItemNo + 1;
        // Calculate 30% of the value
        BigDecimal dtbExciseTax = exciseDutyTransaction.getAmount().multiply(new BigDecimal("30")).divide(new BigDecimal("100"));
        dtbExciseTax = dtbExciseTax.setScale(2, RoundingMode.HALF_UP);

        // Calculate 30% of the value
        BigDecimal mcpExciseTax = exciseDutyTransaction.getAmount().multiply(new BigDecimal("70")).divide(new BigDecimal("100"));
        mcpExciseTax = mcpExciseTax.setScale(2, RoundingMode.HALF_UP);

        TransactionDetail child = new TransactionDetail();
        child.setCrAcctNo(entityAccounts.getCollectionAccount());
        child.setDrAcctNo(exciseDutyTransaction.getCrAcctNo());
        child.setAmount(dtbExciseTax);
        child.setTransDescr("URA Excise share" + "-" + request.getTransDescr());
        child.setTransType("EXCISE_DUTY");
        child.setCreatedBy(request.getPostedBy());
        child.setIsoCode(request.getIsoCode());
        child.setEntryType("GL2GL");
        child.setCreateDate(DataUtils.getCurrentTimeStamp());
        child.setPostingDt(DataUtils.getCurrentTimeStamp());
        child.setStatus("Pending");
        child.setItemNo(maxItemNo);
        child.setMainTransId(request.getId());
        transactionDetailList.add(child);

        child = new TransactionDetail();
        child.setCrAcctNo(entityAccounts.getExciseTaxAccount());
        child.setDrAcctNo(exciseDutyTransaction.getCrAcctNo());
        child.setAmount(mcpExciseTax);
        child.setTransDescr("URA Excise share" + "-" + request.getTransDescr());
        child.setTransType("EXCISE_DUTY");
        child.setCreatedBy(request.getPostedBy());
        child.setIsoCode(request.getIsoCode());
        child.setEntryType("GL2GL");
        child.setCreateDate(DataUtils.getCurrentTimeStamp());
        child.setPostingDt(DataUtils.getCurrentTimeStamp());
        child.setStatus("Pending");
        child.setItemNo(maxItemNo);
        child.setMainTransId(request.getId());
        transactionDetailList.add(child);

//        if (policy.getTrustPostingCategory() != null) {
//            String description, debitAcct, creditAcct, transType;
//            if (policy.getTrustPostingCategory().trim().equals("TRUST_TO_MICROPAY")) {
//                description = "TRUST2MCP_" + child.getTransDescr();
//                transType = "TRUST2MCP_" + policy.getAmountType();
//                debitAcct = micropayGLAccount.getParamValue();
//                creditAcct = trustGLAccount.getParamValue();
//            } else if (policy.getTrustPostingCategory().trim().equals("MICROPAY_TO_TRUST")) {
//                description = "MCP2TRUST~" + child.getTransDescr();
//                transType = "MCP2TRUST~" + policy.getAmountType();
//                debitAcct = trustGLAccount.getParamValue();
//                creditAcct = micropayGLAccount.getParamValue();
//            } else {
//                continue;
//            }
//            transItems.add(TransactionDetail.builder()
//                    .mainTransId(baseTransaction.getId())
//                    .crAcctNo(creditAcct)
//                    .drAcctNo(debitAcct)
//                    .amount(child.getAmount())
//                    .isoCode("UGX")
//                    .status("Pending")
//                    .transType(transType)
//                    .entryType(policy.getTrustPostingCategory())
//                    .transDescr(description)
//                    .postingDt(DataUtils.getCurrentTimeStamp())
//                    .itemNo(policy.getPostingPriority() * -1)
//                    .createDate(DataUtils.getCurrentTimeStamp())
//                    .createdBy(baseTransaction.getPostedBy())
//                    .build());
//        }
    }


    private TransactionDetail generateChildTransactions(TransRequestData request, ServicePostingDetail policy, EntityAccounts entityAccounts, EntityAmount entityAmount) {
        TransactionDetail child = new TransactionDetail();
        request.setAdditionalCharge((request.getAdditionalCharge() == null) ? BigDecimal.ZERO : request.getAdditionalCharge());

        switch (policy.getSourceAccount()) {
            case "AGENT_COMMISSION_ACCOUNT":
            case "SUPER_AGENT_COMMISSION_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getAgentCommission());
                break;
            case "BANK_COMMISSION_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getBankCommission());
                break;
            case "VENDOR_COMMISSION_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getVendorCommissionAccount());
                break;
            case "CRDB_CHARGE_EXPENSE_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getCenteAgentChargeExpenseAccount());
                break;
            case "MAINTENANCE_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getMaintenanceAccount());
                break;
            case "MOBILE_MONEY_TAX_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getMobileMoneyTaxAccount());
                break;
            case "BANK_EXPENSE_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getExpenseAccount());
                break;
            case "COLLECTION_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getCollectionAccount());
                break;
            case "EXCISE_DUTY_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getExciseTaxAccount());
                break;
            case "MICROPAY_COMMISSION_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getMicropayCommission());
                break;
            case "CUSTOMER_ACCOUNT_A":
            case "CUSTOMER_ACCOUNT_B":
            case "AGENT_FLOAT_ACCOUNT_A":
            case "AGENT_FLOAT_ACCOUNT_B":
                child.setDrAcctNo(request.getSourceAcctNo());
                break;
            case "SUSPENSE_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getSuspenseAccount());
                break;
            case "TRANSITORY_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getTransitAccount());
                break;
            case "WITHHOLD_TAX_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getWithholdTaxAccount());
                break;
            case "BANK_OPERATIONS_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getBankOperationAccount());
                break;
            case "BANK_TRUST_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getBankTrustAccount());
                break;
            case "AIRTEL_COMMISSION_RECEIVABLE":
                child.setDrAcctNo(entityAccounts.getAirtelCommissionReceivable());
                break;
            case "INTERSWITCH_COMMISSION_RECEIVABLE":
                child.setDrAcctNo(entityAccounts.getInterSwitchCommissionReceivable());
                break;
            case "LYCAMOBILE_COMMISSION_RECEIVABLE":
                child.setDrAcctNo(entityAccounts.getLycaMobileCommissionReceivable());
                break;
            default:
                child.setDrAcctNo(null);
                break;
        }

        switch (policy.getDestinationAccount()) {
            case "AGENT_COMMISSION_ACCOUNT":
            case "SUPER_AGENT_COMMISSION_ACCOUNT":
                child.setCrAcctNo(entityAccounts.getAgentCommission());
                break;
            case "BANK_COMMISSION_ACCOUNT":
                child.setCrAcctNo(entityAccounts.getBankCommission());
                break;
            case "VENDOR_COMMISSION_ACCOUNT":
                child.setCrAcctNo(entityAccounts.getVendorCommissionAccount());
                break;
            case "CRDB_CHARGE_EXPENSE_ACCOUNT":
                child.setDrAcctNo(entityAccounts.getCenteAgentChargeExpenseAccount());
                break;
            case "MAINTENANCE_ACCOUNT":
                child.setCrAcctNo(entityAccounts.getMaintenanceAccount());
                break;
            case "MOBILE_MONEY_TAX_ACCOUNT":
                child.setCrAcctNo(entityAccounts.getMobileMoneyTaxAccount());
                break;
            case "BANK_EXPENSE_ACCOUNT":
                child.setCrAcctNo(entityAccounts.getExpenseAccount());
                break;
            case "COLLECTION_ACCOUNT":
                child.setCrAcctNo(entityAccounts.getCollectionAccount());
                break;
            case "EXCISE_DUTY_ACCOUNT":
                child.setCrAcctNo(entityAccounts.getExciseTaxAccount());
                break;
            case "MICROPAY_COMMISSION_ACCOUNT":
                child.setCrAcctNo(entityAccounts.getMicropayCommission());
                break;
            case "CUSTOMER_ACCOUNT_A":
            case "CUSTOMER_ACCOUNT_B":
            case "AGENT_FLOAT_ACCOUNT_A":
            case "AGENT_FLOAT_ACCOUNT_B":
                child.setCrAcctNo(request.getDestAcctNo());
                break;
            case "SUSPENSE_ACCOUNT":
                child.setCrAcctNo(entityAccounts.getSuspenseAccount());
                break;
            case "TRANSITORY_ACCOUNT":
                child.setCrAcctNo(entityAccounts.getTransitAccount());
                break;
            case "WITHHOLD_TAX_ACCOUNT":
                child.setCrAcctNo(entityAccounts.getWithholdTaxAccount());
                break;
            case "BANK_OPERATIONS_ACCOUNT":
                child.setCrAcctNo(entityAccounts.getBankOperationAccount());
                break;
            case "BANK_TRUST_ACCOUNT":
                child.setCrAcctNo(entityAccounts.getBankTrustAccount());
                break;
            case "AIRTEL_COMMISSION_RECEIVABLE":
                child.setCrAcctNo(entityAccounts.getAirtelCommissionReceivable());
                break;
            case "INTERSWITCH_COMMISSION_RECEIVABLE":
                child.setCrAcctNo(entityAccounts.getInterSwitchCommissionReceivable());
                break;
            case "LYCAMOBILE_COMMISSION_RECEIVABLE":
                child.setCrAcctNo(entityAccounts.getLycaMobileCommissionReceivable());
                break;
            default:
                child.setCrAcctNo(null);
                break;
        }

        switch (policy.getAmountType()) {
            case "AGENT_COMMISSION":
            case "TRANS_AMOUNT_AGENT_SHARE":
                child.setAmount(entityAmount.getAgentCommissionAmount());
                break;
            case "NET_CHARGE":
                child.setAmount(entityAmount.getNetCharge());
                break;
            case "VENDOR_COMMISSION":
                child.setAmount(entityAmount.getVendorCommission());
                break;
            case "TRANS_AMOUNT_BANK_SHARE":
                child.setAmount(entityAmount.getBankShare());
                break;
            case "MAINTENANCE_COMMISSION":
                child.setAmount(entityAmount.getMaintenanceAmountShare());
                break;
            case "EXCISE_DUTY":
                child.setAmount(entityAmount.getExciseDuty());
                break;
            case "CHARGE":
                child.setAmount(entityAmount.getTotalCharge());
                break;
            case "TRANS_AMOUNT_VENDOR_SHARE":
                child.setAmount(entityAmount.getVendorShare());
                break;
            case "TRANS_AMOUNT":
                child.setAmount(request.getTransAmt().add(request.getAdditionalCharge()));
                break;
            case "MOBILE_MONEY_TAX":
                child.setAmount(entityAmount.getMobileMoneyTax());
                break;
            case "WITHHOLD_TAX":
                child.setAmount(entityAmount.getWithholdTax());
                break;
            default:
                child.setAmount(new BigDecimal(0.0D));
                break;
        }

        child.setTransDescr(policy.getAmountType() + "-" + request.getDescription());
        child.setTransType(policy.getAmountType());
        child.setCreatedBy(request.getOutletCode());
        child.setIsoCode(request.getCurrency());
        child.setEntryType(policy.getTransCategory());
        child.setCreateDate(DataUtils.getCurrentTimeStamp());
        child.setPostingDt(DataUtils.getCurrentTimeStamp());
        child.setItemNo(policy.getPostingPriority());

        return child;
    }

    private EntityAccounts getEntityAccounts(TransRequestData request, ServiceRef serviceRef) throws MediumException {
        EntityAccounts response = new EntityAccounts();
        MobileUserAccount agentCommissionAccountData = null;

        if (!request.getOutletCode().equals("SYSTEM")) {
            agentCommissionAccountData = this.mobileUserAccountRepo.findAgentCommissionAccount(request.getOutletCode());
        }

        response.setBankCommission(serviceRef.getBankIncomeAcctNo());
        response.setCollectionAccount(serviceRef.getBillerAcctNo());

        SystemParameter systemParameter = this.systemParameterRepo.getParameter("S09");
        if (systemParameter == null) {
            throw new MediumException(ErrorData.builder().code("404").message("No matches found for the specified parameter code").build());
        }
        response.setMicropayCommission(systemParameter.getParamValue());

        systemParameter = this.systemParameterRepo.getParameter("S10");
        if (systemParameter == null) {
            throw new MediumException(ErrorData.builder().code("404").message("No matches found for the specified parameter code").build());
        }
        response.setExpenseAccount(systemParameter.getParamValue());

        response.setAgentCommission(agentCommissionAccountData == null ? null : agentCommissionAccountData.getAcctNo());
        response.setTransitAccount(serviceRef.getTransitAcctNo());
        response.setMaintenanceAccount(serviceRef.getMaintenanceAccount());
        response.setVendorCommissionAccount(serviceRef.getExpenseAcctNo());

        systemParameter = this.systemParameterRepo.getParameter("S11");
        if (systemParameter == null) {
            throw new MediumException(ErrorData.builder().code("404").message("No matches found for the specified parameter code").build());
        }
        response.setWithholdTaxAccount(systemParameter.getParamValue());

        systemParameter = this.systemParameterRepo.getParameter("S25");
        if (systemParameter == null) {
            throw new MediumException(ErrorData.builder().code("404").message("No matches found for the specified parameter code").build());
        }
        response.setCenteAgentChargeExpenseAccount(systemParameter.getParamValue());

        systemParameter = this.systemParameterRepo.getParameter("S12");
        if (systemParameter == null) {
            throw new MediumException(ErrorData.builder().code("404").message("No matches found for the specified parameter code").build());
        }
        response.setExciseTaxAccount(systemParameter.getParamValue());

        systemParameter = this.systemParameterRepo.getParameter("S07");
        if (systemParameter == null) {
            throw new MediumException(ErrorData.builder().code("404").message("No matches found for the specified parameter code").build());
        }
        response.setSuspenseAccount(systemParameter.getParamValue());

        systemParameter = this.systemParameterRepo.getParameter("S23");
        if (systemParameter == null) {
            throw new MediumException(ErrorData.builder().code("404").message("No matches found for the specified parameter code").build());
        }
        response.setMobileMoneyTaxAccount(systemParameter.getParamValue());

        systemParameter = this.systemParameterRepo.getParameter("S28");
        if (systemParameter == null) {
            throw new MediumException(ErrorData.builder().code("404").message("No matches found for the specified parameter code").build());
        }
        response.setBankOperationAccount(systemParameter.getParamValue());

        systemParameter = this.systemParameterRepo.getParameter("S17");
        if (systemParameter == null) {
            throw new MediumException(ErrorData.builder().code("404").message("No matches found for the specified parameter code").build());
        }
        response.setBankTrustAccount(systemParameter.getParamValue());

        systemParameter = this.systemParameterRepo.getParameter("S29");
        if (systemParameter == null) {
            throw new MediumException(ErrorData.builder().code("404").message("No matches found for the specified parameter code").build());
        }
        response.setAirtelCommissionReceivable(systemParameter.getParamValue());
        systemParameter = this.systemParameterRepo.getParameter("S30");
        if (systemParameter == null) {
            throw new MediumException(ErrorData.builder().code("404").message("No matches found for the specified parameter code").build());
        }
        response.setInterSwitchCommissionReceivable(systemParameter.getParamValue());
        systemParameter = this.systemParameterRepo.getParameter("S31");
        if (systemParameter == null) {
            throw new MediumException(ErrorData.builder().code("404").message("No matches found for the specified parameter code").build());
        }
        response.setLycaMobileCommissionReceivable(systemParameter.getParamValue());
        return response;
    }

    private Boolean isAgentAndOutletRelated(TransRequestData request) {
        if (request.getServiceCode() != 70022)
            return false;

        Integer sourceCompanyId = mobileUserAccountRepo.findCompanyIdByAccount(request.getSourceAcctNo());
        Integer destCompanyId = mobileUserAccountRepo.findCompanyIdByAccount(request.getDestAcctNo());

        // Check for nulls before comparing
        if (sourceCompanyId == null || destCompanyId == null) {
            return false; // Return false if either is null
        }
        return sourceCompanyId.equals(destCompanyId);
    }


    private EntityAmount getEntityAmount(TransRequestData request, ServiceRef serviceRef, List<ServicePostingDetail> postingPolicies) throws MediumException {
        EntityAmount response = new EntityAmount();
        BigDecimal mobileMoneyTax = BigDecimal.ZERO;
        BigDecimal maintenanceAmount = BigDecimal.ZERO;

        SystemParameter systemParameter = this.systemParameterRepo.getParameter("S20");
        if (systemParameter == null) {
            throw new MediumException(ErrorData.builder().code("404").message("Unable to determine excise duty due to Invalid parameter code specified").build());
        }
        String strExciseDutyPercentage = systemParameter.getParamValue();

        systemParameter = this.systemParameterRepo.getParameter("S22");
        if (systemParameter == null) {
            throw new MediumException(ErrorData.builder().code("404").message("Unable to determine mobile money tax due to Invalid parameter code specified").build());
        }

        systemParameter = this.systemParameterRepo.getParameter("S24");
        if (systemParameter == null) {
            throw new MediumException(ErrorData.builder().code("404").message("Unable to determine withhold tax due to Invalid parameter code specified").build());
        }
        String strWithHoldPercentage = systemParameter.getParamValue();

        BigDecimal chargeAmount = this.productChargeService.getServiceCharge(serviceRef.getServiceId(), request.getTransAmt(), new BigDecimal(strExciseDutyPercentage));
        if (serviceRef.getMobileMoneyTaxPercentage() != null) {
            mobileMoneyTax = request.getTransAmt().multiply(serviceRef.getMobileMoneyTaxPercentage());
        }

        BigDecimal exciseDuty = chargeAmount.multiply(new BigDecimal(strExciseDutyPercentage));
        System.out.println("SERVICE CODE: " + request.getServiceCode() + " Service Id " + serviceRef.getServiceId() + " Transaction amount " + request.getTransAmt());

        ServiceCommission agentCommission = this.productCommissionService.getServiceCommission(serviceRef.getServiceId(), request.getTransAmt(), chargeAmount);
        System.out.println("SERVICE CODE: " + request.getServiceCode() + " Commission amount " + agentCommission);

        if (!isAgentAndOutletRelated(request)) {
            response.setAgentCommissionAmount(agentCommission.getAmount());
            response.setWithholdTax(agentCommission.getAmount().multiply(new BigDecimal(strWithHoldPercentage)));
        }
        response.setVendorCommission(agentCommission.getVendorShare());

        for (ServicePostingDetail policy : postingPolicies) {
            switch (policy.getAmountType()) {
                case "TRANS_AMOUNT_VENDOR_SHARE":
                    response.setVendorShare(request.getTransAmt().multiply(policy.getTranAmtVendorShare()));
                    break;
                case "TRANS_AMOUNT_BANK_SHARE":
                    response.setBankShare(request.getTransAmt().multiply(policy.getTranAmtBankShare()));
                    break;
                case "TRANS_AMOUNT_AGENT_SHARE":
                    response.setAgentCommissionAmount(request.getTransAmt().multiply(policy.getTranAmtAgentShare()));
                    break;
            }
        }

        BigDecimal micropayCommission = chargeAmount.subtract(agentCommission.getAmount().add(agentCommission.getVendorShare()));
        response.setExciseDuty(exciseDuty);
        response.setNetCharge(micropayCommission);
        response.setTotalCharge(chargeAmount);
        response.setMobileMoneyTax(mobileMoneyTax);

        if (serviceRef.getMaintainCommissionPercentage() != null && serviceRef.getMaintenanceCalculationBasis() != null) {
            if (serviceRef.getMaintenanceCalculationBasis().equalsIgnoreCase("MICROPAY_COMMISSION")) {
                maintenanceAmount = serviceRef.getMobileMoneyTaxPercentage().multiply(micropayCommission);
            } else if (serviceRef.getMaintenanceCalculationBasis().equalsIgnoreCase("AGENT_COMMISSION")) {
                maintenanceAmount = serviceRef.getMobileMoneyTaxPercentage().multiply(agentCommission.getAmount());
            }
        }
        response.setMaintenanceAmountShare(maintenanceAmount);
        System.out.println("=============== Micropay Commission: " + micropayCommission);
        System.out.println("=============== Agent Commission: " + agentCommission);
        System.out.println("=============== Total Charge: " + chargeAmount);
        System.out.println("=============== Mobile Money Tax: " + mobileMoneyTax);
        System.out.println("=============== Maintenance Fee: " + maintenanceAmount);
        System.out.println("=============== Excise Duty Tax: " + exciseDuty);

        return response;
    }


    public EntityAmount findCharges(TransRequestData request) throws MediumException {
        EntityAmount response = new EntityAmount();
        Logger.logEvent("Starting findCharges for serviceCode: {}", request.getServiceCode());

        // Fetch the service reference by code
        ServiceRef serviceRef = this.productService.findServiceByCode(request.getServiceCode());
        if (serviceRef == null) {
            Logger.logEvent("Service reference not found for code: {}", request.getServiceCode());
            throw new MediumException(ErrorData.builder().code("404").message("Invalid service code specified").build());
        }
        Logger.logEvent("Service reference found: {}", serviceRef);

        // Fetch excise duty percentage parameter
        SystemParameter systemParameter = this.systemParameterRepo.getParameter("S20");
        if (systemParameter == null) {
            Logger.logInfo("Excise duty parameter not found for code: S20");
            throw new MediumException(ErrorData.builder().code("404").message("Unable to determine excise duty due to Invalid parameter code specified").build());
        }
        String strExciseDutyPercentage = systemParameter.getParamValue();
        Logger.logEvent("Excise duty percentage: {}", strExciseDutyPercentage);

        // Fetch withhold tax parameter
        systemParameter = this.systemParameterRepo.getParameter("S24");
        if (systemParameter == null) {
            Logger.logInfo("Withhold tax parameter not found for code: S24");
            throw new MediumException(ErrorData.builder().code("404").message("Unable to determine withhold tax due to Invalid parameter code specified").build());
        }
        String strWithHoldPercentage = systemParameter.getParamValue();
        Logger.logEvent("Withhold tax percentage: {}", strWithHoldPercentage);

        // Calculate charge amount based on service charge
        BigDecimal charge = this.productChargeService.getServiceCharge(
                serviceRef.getServiceId(),
                request.getTransAmt(),
                new BigDecimal(strExciseDutyPercentage)
        );
        Logger.logEvent("Calculated service charge: {}", charge);

        // Calculate mobile money tax
        BigDecimal mobileMoneyTax = BigDecimal.ZERO;
        if (serviceRef.getMobileMoneyTaxPercentage() != null) {
            mobileMoneyTax = request.getTransAmt().multiply(serviceRef.getMobileMoneyTaxPercentage());
            Logger.logEvent("Mobile money tax calculated: {}", mobileMoneyTax);
        }

        // Calculate excise duty
        BigDecimal exciseDuty = BigDecimal.ZERO;
        Logger.logEvent("Excise duty calculated: {}", exciseDuty);

        List<ServicePostingDetail> postingPolicies = servicePostingDetailRepo.findServicePostingPolicies(serviceRef.getServiceId());
        if (postingPolicies.isEmpty()) {
            Logger.logEvent("No transaction policies found for serviceId: {}", serviceRef.getServiceId());
            throw new MediumException(ErrorData.builder().code("404").message("No Transaction Policy exists for the transaction type").build());
        }
        Logger.logEvent("Posting policies retrieved: {}", postingPolicies);

        BigDecimal chargeAmount = BigDecimal.ZERO;
        BigDecimal netCharge;
        for (ServicePostingDetail postingDetail : postingPolicies) {
            Logger.logEvent("Processing posting detail: {}", postingDetail);
            if ("EXCISE_DUTY".equals(postingDetail.getAmountType())) {
                exciseDuty = charge.multiply(new BigDecimal(strExciseDutyPercentage));
                Logger.logEvent("Excise duty updated for policy: {}", exciseDuty);
            }
            if ("CHARGE".equals(postingDetail.getAmountType())) {
                chargeAmount = charge;
                Logger.logEvent("Charge amount set for policy: {}", chargeAmount);
            }
        }

        // Fetch service commission
        ServiceCommission commissionAmount = this.productCommissionService.getServiceCommission(
                serviceRef.getServiceId(),
                request.getTransAmt(),
                chargeAmount
        );
        Logger.logEvent("Service commission calculated: {}", commissionAmount);

        netCharge = chargeAmount.subtract(commissionAmount.getAmount().add(commissionAmount.getVendorShare()));
        Logger.logEvent("Net charge calculated: {}", netCharge);

        // Set the response data
        response.setAgentCommissionAmount(commissionAmount.getAmount());
        response.setWithholdTax(commissionAmount.getAmount().multiply(new BigDecimal(strWithHoldPercentage)));
        response.setNetCharge(netCharge);
        response.setExciseDuty(exciseDuty);
        response.setTotalCharge(chargeAmount.add(exciseDuty));
        response.setMobileMoneyTax(mobileMoneyTax);

        Logger.logEvent("findCharges completed successfully with response: {}", response);
        return response;
    }


    @Transactional
    public TxnResult reverseTrans(TransReversalData request) {
        List<TransDetails> transDetailsList = new ArrayList<>();
        TransRequestData transRequestData = new TransRequestData();

        TransactionRef transactionDetail = this.transactionRefRepo.findTransactionRef(request.getOriginTranId());
        if (transactionDetail == null) {
            throw new MediumException(ErrorData.builder().code("-99").message("Could not find transaction details for the specified transaction Id").build());
        }
        List<TransactionDetail> transactionDetailList = this.transactionDetailRepo.findPostedTransactionDetails(request.getOriginTranId());
        if (transactionDetailList == null || transactionDetailList.isEmpty()) {
            throw new MediumException(ErrorData.builder().code("-99").message("Could not find child Items for the specified transaction Id").build());
        }
        transRequestData.setSourceAcctNo(transactionDetail.getCrAcctNo().trim());
        transRequestData.setDestAcctNo(transactionDetail.getDrAcctNo().trim());
        transRequestData.setChannelCode("SUPER_ADMIN");
        for (int i = 0; i < transactionDetailList.size(); i++) {
            String sourceAcctNo = transactionDetailList.get(i).getCrAcctNo();
            String destAcctNo = transactionDetailList.get(i).getDrAcctNo();
            transDetailsList.add(TransDetails.builder().mcpTransDetailId(transactionDetailList.get(i).getId()).transAmt(transactionDetailList.get(i).getAmount()).sourceAcctNo(sourceAcctNo).destAcctNo(destAcctNo).mcpTransId(request.getOriginTranId()).amountType(transactionDetailList.get(i).getTransType()).description("Rev**" + transactionDetailList.get(i).getTransDescr().trim()).transType(StringUtil.getTransferType(sourceAcctNo, destAcctNo)).build());
        }

        if (transactionDetail.getTransactionMode().equals("COMPLETED")) {
            TxnResult transactionResponse = this.equiWebService.fundsTransfer(transRequestData, transDetailsList, true);
            if (!transactionResponse.getCode().equals("00")) {
                return TxnResult.builder().message(transactionResponse.getMessage()).code(transactionResponse.getCode()).build();
            }
        }
        String status = "REVERSED";
        this.transactionRefRepo.updateTransactionStatus(request.getOriginTranId(), "N", "N", "Y", request.getReversalReason(), status);
        this.transCenteTrustRepo.deleteCenteTrustTransactionByTransId(request.getOriginTranId());

        return TxnResult.builder().message("approved").code("00").build();
    }


    @Transactional
    public void sendCommissionAlertMessage(String outletPhoneNo, BigDecimal commissionAmount, String serviceTypeDesc, String commissionAccount) {
        if (commissionAmount != null) {
            if (commissionAccount != null) {
                try {
                    AccountRequest accountRequest = new AccountRequest();
                    accountRequest.setAcctNo(commissionAccount);
                    TxnResult accountBalanceResponse = this.equiWebService.doAccountBalance(accountRequest);
                    if (!accountBalanceResponse.getCode().equals("00")) {
                        Logger.logError(accountBalanceResponse.getMessage() + " during commission balance Inquiry");
                    } else {
                        CIAccountBalance ciAccountBalance = (CIAccountBalance) accountBalanceResponse.getData();
                        String messageText = "Micropay. You have earned commission of UGX " + StringUtil.toCurrencyFormat(commissionAmount) + " on " + serviceTypeDesc + ". \nYour commission balance is UGX " + StringUtil.toCurrencyFormat(ciAccountBalance.getAvailableBalance()) + ". Thank you.";
                        MessageOutbox messageOutbox = new MessageOutbox();
                        messageOutbox.setRecipientNumber(outletPhoneNo);
                        messageOutbox.setMessageText(messageText);
                        messageOutbox.setDeliverSMS(false);
                        this.messageService.logSMS(messageOutbox);
                    }
                } catch (Exception e) {
                    Logger.logError(e);
                }
            }
        }
    }

    @Transactional
    public TxnResult findAccountStatement(CIStatementRequest request) {
        if (request.getStatementType().equals("MINI")) {
            Date endDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.add(Calendar.YEAR, -3);
            Date fromDate = calendar.getTime();
            request.setFromDate(fromDate);
            request.setToDate(endDate);
        }

        TxnResult statementResult = this.equiWebService.doAccountFullStatement(request);
        if (!statementResult.getCode().equals("00")) return statementResult;

        if (request.getStatementType().equals("FULL")) {
            TransRequestData data = (TransRequestData) CommonService.getCommonRequest(new TransRequestData(), request.getApiUserName(), request.getDeviceId(), request.getUserPhoneNo(), request.getOutletCode(), request.getPinNo(), request.getChannelCode());
            data.setCurrency("UGX");
            data.setTransAmt(BigDecimal.valueOf(300L));
            data.setServiceCode(70072);
            data.setSourceAcctNo(request.getAccountNo());
            data.setDescription("Account Statement Charge");
            TxnResult txnResult = this.fundsTransfer(data);
            if (!txnResult.getCode().equals("00")) {
                throw new MediumException(ErrorData.builder().code(txnResult.getCode()).message(txnResult.getMessage()).build());
            }
        }
        return statementResult;
    }

    @Transactional
    public TxnResult doAccountBalance(AccountRequest request) {
        TxnResult accountResult = this.equiWebService.doAccountBalance(request);
        return accountResult;
    }

    private void logCenteTrustTrans(TransactionDetail detail, ServicePostingDetail policy, String trustAccount, String micropayOperationAccount) {
        if (policy.getTrustPostingCategory() != null) {
            if (policy.getPostingPriority() > 0) {
                String drAcctNo;
                String crAcctNo;
                if (policy.getTrustPostingCategory().trim().equals("TRUST_TO_MICROPAY")) {
                    drAcctNo = trustAccount;
                    crAcctNo = micropayOperationAccount;
                } else {
                    drAcctNo = micropayOperationAccount;
                    crAcctNo = trustAccount;
                }

                String referenceNo = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
                TransCenteTrust centeTrust = new TransCenteTrust(detail.getMainTransId(), drAcctNo, crAcctNo, detail.getAmount(), detail.getTransDescr(), detail.getCreatedBy(), "PENDING", policy.getTrustPostingCategory(), referenceNo);
                centeTrust.setStatus("PENDING");
                this.transCenteTrustRepo.save(centeTrust);
            }
        }
    }

    @Transactional
    public void processPendingTransactions() throws MediumException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String processId = UUID.randomUUID().toString();
        Logger.logInfo("[" + processId + "] Starting processPendingTransactions at " + formatter.format(new Date()));

        List<TransactionDetail> transactionDetailList = this.reportRepo.findPendingTransactionsWithSuccessFlagY();
        if (transactionDetailList != null && !transactionDetailList.isEmpty()) {
            Logger.logInfo("[" + processId + "] Found " + transactionDetailList.size() + " pending transactions to process.");

            for (TransactionDetail transactionDetail : transactionDetailList) {
                String transactionId = transactionDetail.getId().toString();
                String mainTransId = transactionDetail.getMainTransId().toString();

//                    Logger.logInfo("[" + processId + " | Transaction ID: " + transactionId + " | Main Trans ID: " + mainTransId
//                            + "] Processing transaction.");

                // Retrieve initiator phone and prepare transfer details
                String initiatorPhone = transactionDetail.getInitiatorPhoneNo();
                TransDetails transDetails = TransDetails.builder().transAmt(transactionDetail.getAmount()).sourceAcctNo(transactionDetail.getDrAcctNo()).destAcctNo(transactionDetail.getCrAcctNo()).description(transactionDetail.getTransDescr()).mcpTransId(transactionDetail.getMainTransId()).transType(StringUtil.getTransferType(transactionDetail.getDrAcctNo(), transactionDetail.getCrAcctNo())).build();

//                    Logger.logInfo("[" + processId + " | Transaction ID: " + transactionId + " | Main Trans ID: " + mainTransId
//                            + "] Sending funds transfer request to eQuiWeb with details: " + transDetails);
                try {
                    TxnResult result = this.equiWebService.fundsTransfer(transDetails);
                    String status = result.getCode().equals("00") ? "Posted" : "Pending";
                    Logger.logInfo("[" + processId + " | Transaction ID: " + transactionId + " | Main Trans ID: " + mainTransId + "] Funds transfer result - Status: " + status + ", Message: " + result.getMessage());

                    TransRespData respData = null;
                    if (result.getCode().equals("00")) {
                        respData = (TransRespData) result.getData();
                    }
                    this.transactionRefRepo.updateTxnDetailStatus(transactionDetail.getId(), "N", result.getMessage(), status, respData != null ? respData.getCbsTransId() : "0");

                    if (result.getCode().equals("00") &&
                            ("AGENT_COMMISSION".equals(transactionDetail.getTransType()) ||
                                    "TRANS_AMOUNT_AGENT_SHARE".equals(transactionDetail.getTransType()))) {

                        Logger.logInfo("[" + processId + " | Transaction ID: " + transactionId +
                                " | Main Trans ID: " + mainTransId + "] AGENT_COMMISSION detected. Sending commission alert to " + initiatorPhone);

                        ExecutorService executorService = Executors.newSingleThreadExecutor();  // Using a single-thread executor for simplicity
                        executorService.execute(() -> sendCommissionAlertMessage(initiatorPhone,
                                transactionDetail.getAmount(),
                                transactionDetail.getTransDescr(),
                                transactionDetail.getCrAcctNo()));

                        executorService.shutdown();  // Shutdown the executor service
                    }


                } catch (Exception e) {
                    Logger.logError("[" + processId + " | Transaction ID: " + transactionId + " | Main Trans ID: " + mainTransId + "] Error processing transaction => " + e);
                }
            }
        } else {
            Logger.logInfo("[" + processId + "] No transaction details found for background posting at " + formatter.format(DataUtils.getCurrentTimeStamp()));
        }
        Logger.logInfo("[" + processId + "] Completed processPendingTransactions at " + formatter.format(new Date()));
    }

}
