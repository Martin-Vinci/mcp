package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.MobileUser;
import com.greybox.mediums.entities.TransactionRef;
import com.greybox.mediums.entities.TransactionVoucher;
import com.greybox.mediums.entities.User;
import com.greybox.mediums.models.SearchCriteria;
import com.greybox.mediums.models.TransactionBand;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.ReportService;
import com.greybox.mediums.utils.CommonResponse;
import com.greybox.mediums.utils.Logger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.greybox.mediums.utils.Logger.logError;
import static com.greybox.mediums.utils.Logger.logInfo;

@RestController
@RequestMapping("/api/v1/reports")
@Api(tags = "Reports Services")
public class ReportEndpoint {

    @Autowired
    private ReportService reportService;

    @PostMapping("/findTransactions")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findTransactions(@RequestBody TransactionRef request) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
            System.out.println(dateFormat.format(new Date()) + " =========== Request received from the endpoint");
            logInfo(request);
            TxnResult txnResult = reportService.findTransactions(request);
            System.out.println(dateFormat.format(new Date()) + " =========== Response dispatched from the endpoint");
            return txnResult;
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findActiveAgents")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findActiveAgents(@RequestBody MobileUser request) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
            System.out.println(dateFormat.format(new Date()) + " =========== Request received from the endpoint");
            logInfo(request);
            TxnResult txnResult = reportService.findActiveAgents(request);
            System.out.println(dateFormat.format(new Date()) + " =========== Response dispatched from the endpoint");
            return txnResult;
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findTransactionBands")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findTransactionBands(@RequestBody TransactionBand request) {
        try {
            logInfo(request);
            TxnResult txnResult = reportService.findTransactionBands(request);
            return txnResult;
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findDashboardStatistics")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findDashboardStatistics(@RequestBody SearchCriteria request) {
        try {
            logInfo(request);
            TxnResult txnResult = reportService.findDashboardStatistics(request);
            return txnResult;
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }


    @PostMapping("/findAgentFloatLevels")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findAgentFloatLevels(@RequestBody SearchCriteria request) {
        try {
            logInfo(request);
            TxnResult txnResult = reportService.findAgentFloatLevels(request);
            return txnResult;
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findCustomerAccountBalanceLevels")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findCustomerAccountBalanceLevels(@RequestBody SearchCriteria request) {
        try {
            logInfo(request);
            TxnResult txnResult = reportService.findCustomerAccountBalanceLevels(request);
            return txnResult;
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findActiveCustomerCategories")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findActiveCustomerCategories(@RequestBody SearchCriteria request) {
        try {
            logInfo(request);
            TxnResult txnResult = reportService.findActiveCustomerCategories(request);
            return txnResult;
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findActiveAgentsByTransactions")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findActiveAgentsByTransactions(@RequestBody SearchCriteria request) {
        try {
            logInfo(request);
            TxnResult txnResult = reportService.findActiveAgentsByTransactions(request);
            return txnResult;
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findUserAccountsByCategoryAndFloatLevels")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findUserAccountsByCategoryAndFloatLevels(@RequestBody SearchCriteria request) {
        try {
            logInfo(request);
            TxnResult txnResult = reportService.findUserAccountsByCategoryAndFloatLevels(request);
            return txnResult;
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findMobileUsersByGender")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findMobileUsersByGender(@RequestBody SearchCriteria request) {
        try {
            logInfo(request);
            TxnResult txnResult = reportService.findMobileUsersByGender(request);
            return txnResult;
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }


    @PostMapping({"/findTransactionSummary"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findTransactionSummary(@RequestBody SearchCriteria request) {
        try {
            Logger.logInfo(request);
            TxnResult txnResult = this.reportService.findTransactionSummary(request);
            return txnResult;
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findTransactionVouchers")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public TxnResult findTransactionVouchers(@RequestBody TransactionVoucher request) {
        try {
            logInfo(request);
            TxnResult txnResult = reportService.findTransactionVouchers(request);
            return txnResult;
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findCustomers")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public TxnResult findCustomers(@RequestBody MobileUser request) {
        try {
            logInfo(request);
            TxnResult txnResult = reportService.findCustomers(request);
            return txnResult;
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findUsers")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public TxnResult findUsers(@RequestBody User request) {
        try {
            logInfo(request);
            TxnResult txnResult = reportService.findUsers(request);
            return txnResult;
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }


    @PostMapping("/findAgentWithHoldingTax")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public TxnResult findAgentWithHoldingTax(@RequestBody MobileUser request) {
        try {
            logInfo(request);
            TxnResult txnResult = reportService.findAgentWithHoldingTax(request);
            return txnResult;
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }











}
