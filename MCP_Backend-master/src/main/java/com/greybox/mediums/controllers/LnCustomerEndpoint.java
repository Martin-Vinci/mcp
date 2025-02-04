package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.mobile_loan.LnCustomer;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.MessageService;
import com.greybox.mediums.services.mobile_loan.LnCustomerService;
import com.greybox.mediums.utils.CommonResponse;
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

import static com.greybox.mediums.utils.Logger.logError;
import static com.greybox.mediums.utils.Logger.logInfo;

@RestController
@RequestMapping("/api/v1/webcash")
@Api(tags = "Customer Service")
public class LnCustomerEndpoint {
    @Autowired
    private LnCustomerService lnCustomerService;

    @PostMapping("/findCustomerById")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findCustById(@RequestBody LnCustomer request) {
        try {
            logInfo(request);
            return lnCustomerService.findCustById(request);
        }catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findAllCustomers")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findAllCustomers(@RequestBody LnCustomer request) {
        try {
            logInfo(request);
            return lnCustomerService.findAll(request);
        }catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/signUp")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult signUp(@RequestBody LnCustomer request) {
        try {
            logInfo(request);
            if (request.getEdit())
                return lnCustomerService.update(request);
            else
                return lnCustomerService.save(request);
        }
        catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        }
        catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }
}
