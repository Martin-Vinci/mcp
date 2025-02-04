package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.mobile_loan.LnCustomer;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.mobile_loan.LnCustomerService;
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

@RestController
@RequestMapping("/api/v1/webcash")
@Api(tags = "Authentication services")
public class LnAuthenticationEndpoint {

    @Autowired
    private LnCustomerService lnCustomerService;

    @PostMapping("/loginUser")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Manages user authentication in the system")
    public TxnResult authentication(@RequestBody LnCustomer request) {
        try {
            return lnCustomerService.authentication(request);
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

}
