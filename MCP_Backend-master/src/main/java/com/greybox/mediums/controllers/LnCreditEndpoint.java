package com.greybox.mediums.controllers;


import com.greybox.mediums.entities.mobile_loan.LnAccount;
import com.greybox.mediums.entities.mobile_loan.LnCreditApp;
import com.greybox.mediums.entities.mobile_loan.LnSchedule;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.mobile_loan.LnAccountService;
import com.greybox.mediums.services.mobile_loan.LnCreditService;
import com.greybox.mediums.services.mobile_loan.LnScheduleService;
import com.greybox.mediums.services.mobile_loan.LoanManagerService;
import com.greybox.mediums.utils.CommonResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.greybox.mediums.utils.Logger.logError;
import static com.greybox.mediums.utils.Logger.logInfo;
@RestController
@RequestMapping("/api/v1/webcash")
@Api(tags = "Credit Service")
public class LnCreditEndpoint {
    @Autowired
    private LnCreditService customerService;
    @Autowired
    private LoanManagerService loanManagerService;
    @Autowired
    private LnAccountService lnAccountService;
    @Autowired
    private LnScheduleService lnScheduleService;

    @PostMapping("/findAllCreditApplications")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findAllCreditApplications(@RequestBody LnCreditApp request) {
        try {
            logInfo(request);
            return customerService.findAll(request);
        }catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }


    @PostMapping("/creditApplication")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult creditApplication(@RequestBody LnCreditApp request) {
        try {
            logInfo(request);
            return customerService.save(request);
        }catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/approveLoan")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Approves Credit Application")
    public TxnResult approveLoan(@RequestBody LnCreditApp request) {
        try {
            logInfo(request);
            return loanManagerService.approveLoan(request);
        }catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/disburseLoan")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Approves Credit Application")
    public TxnResult disburseLoan(@RequestBody LnAccount request) {
        try {
            logInfo(request);
            return lnAccountService.disburseLoan(request);
        }catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findLoanAccount")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findLoanAccount(@RequestBody LnAccount request) {
        try {
            logInfo(request);
            return lnAccountService.findAll(request);
        }catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findLoanSchedule")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findLoanSchedule(@RequestBody LnSchedule request) {
        try {
            logInfo(request);
            return lnScheduleService.findAll(request);
        }catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

}
