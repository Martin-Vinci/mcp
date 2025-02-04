package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.CashoutInitiation;
import com.greybox.mediums.entities.EscrowPendingTrans;
import com.greybox.mediums.entities.TransCenteTrust;
import com.greybox.mediums.entities.TransactionRef;
import com.greybox.mediums.entities.TransactionVoucher;
import com.greybox.mediums.models.TransRequestData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.CashOutInitiationService;
import com.greybox.mediums.services.EscrowService;
import com.greybox.mediums.services.OutletWithdrawInitiationService;
import com.greybox.mediums.services.TransactionService;
import com.greybox.mediums.services.VoucherTransactionService;
import com.greybox.mediums.utils.CommonResponse;
import com.greybox.mediums.utils.Logger;
import com.greybox.mediums.utils.MediumException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/transaction"})
@Api(tags = {"Transaction Services"})
public class TransactionEndPoint {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private EscrowService escrowService;
    @Autowired
    private CashOutInitiationService cashOutInitiationService;
    @Autowired
    private OutletWithdrawInitiationService outletWithdrawInitiationService;
    @Autowired
    private VoucherTransactionService voucherTransactionService;

    @PostMapping({"/findTransVouchers"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Find Transactions for a report")
    public TxnResult findTransVouchers(@RequestBody TransactionVoucher request) {
        try {
            Logger.logInfo(request);
            return this.voucherTransactionService.findTransVouchers(request);
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findTransactions"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Find Transactions for a report")
    public TxnResult findTransactions(@RequestBody TransactionRef request) {
        try {
            Logger.logInfo(request);
            return this.transactionService.findTransactions(request);
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findTransCenteTrustDetails"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    public TxnResult findTransCenteTrustDetails(@RequestBody TransactionRef request) {
        try {
            Logger.logInfo(request);
            return this.transactionService.findTransCenteTrustDetails(request);
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findTransCenteTrustSummary"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    public TxnResult findTransCenteTrustSummary(@RequestBody TransactionRef request) {
        try {
            Logger.logInfo(request);
            return this.transactionService.findTransCenteTrustSummary(request);
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findCenteTrustByTransId"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    public TxnResult findTransCenteTrustByTransId(@RequestBody TransCenteTrust request) {
        try {
            Logger.logInfo(request);
            return this.transactionService.findTransCenteTrust(request);
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findEscrowTransactions"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Find Transactions for a report")
    public TxnResult findEscrowTransactions(@RequestBody EscrowPendingTrans request) {
        try {
            Logger.logInfo(request);
            return this.escrowService.findEscrowTransactions(request);
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findTransactionDetails"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findTransactionDetails(@RequestBody Long transId) {
        try {
            return this.transactionService.findTransactionDetails(transId);
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/postTransaction"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult postTransaction(@RequestBody TransRequestData request) {
        try {
            return this.transactionService.fundsTransfer(request);
        } catch (MediumException e1) {
            Logger.logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/completeTransaction"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult completeTransaction(@RequestBody TransRequestData request) {
        try {
            return this.transactionService.completeTransaction(request);
        } catch (MediumException e1) {
            Logger.logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/initiateCashOut"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult initiateCashOut(@RequestBody CashoutInitiation request) {
        try {
            return this.cashOutInitiationService.save(request);
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }
}
