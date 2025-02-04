package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.BillerNotif;
import com.greybox.mediums.entities.CustomerDetail;
import com.greybox.mediums.entities.EscrowPendingTrans;
import com.greybox.mediums.entities.IssuedReceipt;
import com.greybox.mediums.entities.MessageOutbox;
import com.greybox.mediums.entities.MobileUser;
import com.greybox.mediums.entities.PaymentRequest;
import com.greybox.mediums.models.AccountRequest;
import com.greybox.mediums.models.CashOutRequest;
import com.greybox.mediums.models.OutletAuthRequest;
import com.greybox.mediums.models.PinRequestData;
import com.greybox.mediums.models.TransRequestData;
import com.greybox.mediums.models.TransReversalData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.models.VoucherRequestData;
import com.greybox.mediums.models.equiweb.CIAccount;
import com.greybox.mediums.models.equiweb.CIStatementRequest;
import com.greybox.mediums.services.AgentBankingService;
import com.greybox.mediums.utils.CommonResponse;
import com.greybox.mediums.utils.Logger;
import com.greybox.mediums.utils.MediumException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.v3.oas.annotations.Operation;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/agent-banking"})
@Api(
        tags = {"Agent Banking Services"}
)
public class AgentBankingEndPoint {
    @Autowired
    private AgentBankingService agentBankingService;

    @PostMapping({"/validateVoucher"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult validateVoucher(@RequestBody VoucherRequestData request) {
        try {
            return this.agentBankingService.validateVoucher(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/buyVoucher"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult buyVoucher(@RequestBody VoucherRequestData request) {
        try {
            Logger.logError(request);
            return this.agentBankingService.buyVoucher(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/redeemVoucher"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult redeemVoucher(@RequestBody VoucherRequestData request) {
        try {
            Logger.logError(request);
            return this.agentBankingService.redeemVoucher(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/fundsTransfer"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Find Transactions for a report")
    public TxnResult fundsTransfer(@RequestBody TransRequestData request) {
        TxnResult txnResult = null;
        try {
            txnResult = this.agentBankingService.fundsTransfer(request);
        } catch (MediumException e1) {
            Logger.logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        } finally {
            try {
                Logger.logInfo("Transaction PayLoad Request=====>: " + request + "\n PayLoad Response: " + txnResult);
            } catch (Exception exception) {}
        }

        return txnResult;
    }


    @PostMapping({"/logTransaction"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    public TxnResult logTransaction(@RequestBody TransRequestData request) {
        try {
            return this.agentBankingService.logTransaction(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/completeTransaction"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    public TxnResult completeTransaction(@RequestBody TransRequestData request) {
        Long transactionId = request.getOriginTransId(); // Assume request contains a transaction ID.
        StringBuilder logMessage = new StringBuilder();

        // Collect initial trace log
        logMessage.append("Starting completeTransaction for transactionId: ").append(transactionId).append("\n");

        try {
            // Log when calling the service to complete the transaction
            logMessage.append("Calling agentBankingService to complete transaction for transactionId: ").append(transactionId).append("\n");
            Logger.logInfo(logMessage.toString());

            // Call the service to complete the transaction
            var result = this.agentBankingService.completeTransaction(request);

            // Collect successful completion trace log
            logMessage.append("Transaction completed successfully for transactionId: ").append(transactionId).append("\n");

            return result;
        } catch (MediumException e) {
            // Collect log for exception and error
            logMessage.append("MediumException occurred for transactionId: ").append(transactionId)
                    .append(". Error: ").append(e.getErrorMessage()).append("\n");
            Logger.logEvent(logMessage.toString(), e);

            return CommonResponse.getMediumExceptionError(e.getErrorMessage());
        } catch (Exception e) {
            // Collect log for any unexpected exception
            logMessage.append("Unexpected error occurred for transactionId: ").append(transactionId).append("\n");
            Logger.logEvent(logMessage.toString(), e);

            return CommonResponse.getUndefinedError();
        } finally {
            // Log all accumulated messages in the finally block
            logMessage.append("Completed completeTransaction execution for transactionId: ").append(transactionId).append("\n");
            Logger.logInfo(logMessage.toString()); // Log the final trace
        }
    }


    @PostMapping({"/doAccountFullStatement"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    public TxnResult fundsTransfer(@RequestBody CIStatementRequest request) {
        TxnResult txnResult = null;

        TxnResult var4;
        try {
            txnResult = this.agentBankingService.doAccountFullStatement(request);
            return txnResult;
        } catch (MediumException e) {
            Logger.logError(e);
            var4 = CommonResponse.getMediumExceptionError(e.getErrorMessage());
        } catch (Exception var17) {
            Logger.logError(var17);
            var4 = CommonResponse.getUndefinedError();
            return var4;
        } finally {
            try {
                Logger.logInfo("Statement PayLoad Request=====>: " + request + "\n PayLoad Response: " + txnResult);
            } catch (Exception var15) {
            }

        }

        return var4;
    }

    @PostMapping({"/doAccountBalance"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    public TxnResult fundsTransfer(@RequestBody AccountRequest request) {
        TxnResult txnResult = null;

        TxnResult var4;
        try {
            txnResult = this.agentBankingService.doAccountBalance(request);
            return txnResult;
        } catch (MediumException var16) {
            Logger.logError(var16);
            var4 = CommonResponse.getMediumExceptionError(var16.getErrorMessage());
        } catch (Exception var17) {
            Logger.logError(var17);
            var4 = CommonResponse.getUndefinedError();
            return var4;
        } finally {
            try {
                Logger.logInfo("Account Balance PayLoad Request=====>: " + request + "\n PayLoad Response: " + txnResult);
            } catch (Exception var15) {
            }

        }

        return var4;
    }

    @PostMapping({"/saveEscrowTransaction"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult saveEscrowTransaction(@RequestBody TransRequestData request) {
        try {
            return this.agentBankingService.saveEscrowTransaction(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/approveEscrowTransaction"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult approveEscrowTransaction(@RequestBody EscrowPendingTrans request) {
        try {
            return this.agentBankingService.approveEscrowTransaction(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/reverseTrans"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult reverseTrans(@RequestBody TransReversalData request) {
        try {
            return this.agentBankingService.reverseTrans(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/enrollCustomerByAgent"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult enrollCustomerByAgent(@RequestBody MobileUser request) {
        try {
            return this.agentBankingService.enrollCustomerByAgent(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return var4.getMessage().contains("mobile_users_phone_number_idx") ? TxnResult.builder().code("-99").message("Specified phone number is already registered in the system").build() : CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/generateDeviceActivationCode"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Creates a customer address in the system"
    )
    public TxnResult generateDeviceActivationCode(@RequestBody MobileUser request) {
        try {
            return this.agentBankingService.generateDeviceActivationCode(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/performDevicePairing"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Creates a customer address in the system"
    )
    public TxnResult performDevicePairing(@RequestBody MobileUser request) {
        try {
            return this.agentBankingService.performDevicePairing(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/pinAuthentication"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult pinAuthentication(@RequestBody OutletAuthRequest request) {
        try {
            return this.agentBankingService.pinAuthentication(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/pinChange"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult pinChange(@RequestBody PinRequestData request) {
        try {
            return this.agentBankingService.pinChange(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/pairCustomerDevice"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult pairCustomerDevice(@RequestBody OutletAuthRequest request) {
        try {
            return this.agentBankingService.pairCustomerDevice(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findAccountsByPhoneNo"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult findAccountsByPhoneNo(@RequestBody AccountRequest request) {
        try {
            return this.agentBankingService.findAccountsByPhoneNo(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/sendSMS"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult sendSMS(@RequestBody MessageOutbox request) {
        try {
            return this.agentBankingService.logSMS(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findOutletDetails"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult findOutletDetails(@RequestBody OutletAuthRequest request) {
        try {
            return this.agentBankingService.findOutletDetails(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findSuperAgentDetails"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult findSuperAgentDetails(@RequestBody OutletAuthRequest request) {
        try {
            return this.agentBankingService.findSuperAgentDetails(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findRecipientSMS"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult findRecipientSMS(@RequestBody OutletAuthRequest request) {
        try {
            return this.agentBankingService.findRecipientSMS(request.getUserPhoneNo());
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findTransCharges"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult findTransCharges(@RequestBody TransRequestData request) {
        try {
            return this.agentBankingService.findTransCharges(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/initiateCustomerCashout"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult initiateCustomerCashout(@RequestBody CashOutRequest request) {
        try {
            return this.agentBankingService.initiateCustomerCashout(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/validateCustomerCashoutOTP"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult validateCustomerCashoutOTP(@RequestBody CashOutRequest request) {
        try {
            return this.agentBankingService.validateCustomerCashoutOTP(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/initiateOutletCashout"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult initiateOutletCashout(@RequestBody CashOutRequest request) {
        try {
            return this.agentBankingService.initiateOutletCashout(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/validateOutletCashoutOTP"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult validateOutletCashoutOTP(@RequestBody CashOutRequest request) {
        try {
            return this.agentBankingService.validateOutletCashoutOTP(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/logBillNotification"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult logBillNotification(@RequestBody BillerNotif request) {
        try {
            return this.agentBankingService.logBillNotification(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return var4.getMessage().toLowerCase().contains("biller_notif_third_party_reference_idx") ? TxnResult.builder().code("-99").message("Third party reference cannot be duplicated.").build() : CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/updateBillNotificationStatus"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    @Operation(
            summary = "Find Transactions for a report"
    )
    public TxnResult updateBillNotificationStatus(@RequestBody BillerNotif request) {
        try {
            return this.agentBankingService.updateBillNotificationStatus(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/createCustomer"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    public TxnResult createCustomer(@RequestBody CustomerDetail request) {
        try {
            return this.agentBankingService.createCustomer(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return var4.getMessage().contains("mobile_users_phone_number_idx") ? TxnResult.builder().code("-99").message("Specified phone number is already registered in the system").build() : CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/accountInquiryByPhoneNo"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    public TxnResult accountInquiryByPhoneNo(@RequestBody CIAccount request) {
        try {
            return this.agentBankingService.accountInquiryByPhoneNo(request);
        } catch (MediumException var3) {
            Logger.logError(var3);
            return CommonResponse.getMediumExceptionError(var3.getErrorMessage());
        } catch (Exception var4) {
            Logger.logError(var4);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/saveReceipt"})
    @ApiOperation(
            value = "",
            authorizations = {@Authorization("apiKey")}
    )
    public TxnResult saveReceipt(@RequestBody IssuedReceipt request) {
        try {
            return this.agentBankingService.saveReceipt(request);
        } catch (Exception var3) {
            Logger.logError(var3);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findIssuedReceipt"})
    public TxnResult findIssuedReceipt(@RequestBody IssuedReceipt request) {
        try {
            return this.agentBankingService.findIssuedReceipt(request);
        } catch (Exception var3) {
            Logger.logError(var3);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/requestPayment"})
    public TxnResult requestPayment(@RequestBody PaymentRequest request) {
        try {
            return this.agentBankingService.requestPayment(request);
        } catch (Exception var3) {
            Logger.logError(var3);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/reviewPaymentRequest"})
    public TxnResult reviewPaymentRequest(@RequestBody PaymentRequest request) {
        try {
            return this.agentBankingService.updatePaymentRequest(request);
        } catch (Exception var3) {
            Logger.logError(var3);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findPendingRequest"})
    public TxnResult findPendingRequest(@RequestBody PaymentRequest request) {
        try {
            return this.agentBankingService.findPendingRequest(request);
        } catch (Exception var3) {
            Logger.logError(var3);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findRequestedPayments"})
    public TxnResult findRequestedPayments(@RequestBody PaymentRequest request) {
        try {
            return this.agentBankingService.findRequestedPayments(request);
        } catch (Exception var3) {
            Logger.logError(var3);
            return CommonResponse.getUndefinedError();
        }
    }
}
