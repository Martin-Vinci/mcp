package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.ServiceCharge;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.ProductChargeService;
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
@RequestMapping("/api/v1/agent-banking/service-charge")
@Api(tags = "Product Charge Services")
public class ServiceChargeEndPoint {

    @Autowired
    private ProductChargeService productChargeService;

    @PostMapping("/findServiceCharge")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findServiceCharge(@RequestBody ServiceCharge request) {
        try {
            logInfo(request);
            return productChargeService.find(request);
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/maintainServiceCharge")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult maintainServiceCharge(@RequestBody ServiceCharge request) {
        try {
            return productChargeService.save(request);
        }
        catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }
}
