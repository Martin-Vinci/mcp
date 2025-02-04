package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.ServiceCommission;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.ProductCommissionService;
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
@RequestMapping("/api/v1/agent-banking/service-commission")
@Api(tags = "Product Commission Services")
public class ServicesCommissionEndPoint {

    @Autowired
    private ProductCommissionService productCommissionService;

    @PostMapping("/findServiceCommission")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findServiceCommission(@RequestBody ServiceCommission request) {
        try {
            logInfo(request);
            return productCommissionService.find(request);
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/maintainServiceCommission")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult maintainServiceCommission(@RequestBody ServiceCommission request) {
        try {
            return productCommissionService.save(request);
        }
        catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }
}
