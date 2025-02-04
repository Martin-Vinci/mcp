package com.greybox.mediums.controllers;


import com.greybox.mediums.entities.mobile_loan.LoanRepaymentHistory;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.mobile_loan.RepaymentService;
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
@Api(tags = "Transaction Service")
public class LnRepayEndpoint {
    @Autowired
    private RepaymentService customerService;
    @PostMapping("/findTransHistory")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findTransHistory(@RequestBody LoanRepaymentHistory request) {
        try {
            logInfo(request);
            return customerService.findAll(request);
        }catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/postLoanRepayment")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult postLoanRepayment(@RequestBody LoanRepaymentHistory request) {
        try {
            logInfo(request);
            return customerService.save(request);
        }catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }
}
