package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.Account;
import com.greybox.mediums.entities.CustomerRef;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.CustomerService;
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
@RequestMapping("/api/v1/agent-banking/customer")
@Api(tags = "Customer Services")
public class CustomerEndPoint {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/findCustomer")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findCustomer(@RequestBody CustomerRef request) {
        try {
            logInfo(request);
            return customerService.find(request);
        }catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findAccountsByEntityId")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findAccountsByEntityId(@RequestBody Account request) {
        try {
            logInfo(request);
            return customerService.findAccountsByEntityId(request);
        }catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/maintainCustomer")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult maintainCustomer(@RequestBody CustomerRef request) {
        try {
            if (request.getEdit())
                return customerService.update(request);
            else
                return customerService.save(request);
        }catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }
}
