package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.ServiceCommissionTier;
import com.greybox.mediums.models.CommissionTierData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.ProductCommissionTierService;
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
@RequestMapping("/api/v1/agent-banking/service-commission-tier")
@Api(tags = "Product Commission Tier Services")
public class ServicesCommissionTierEndPoint {

    @Autowired
    private ProductCommissionTierService productCommissionTierService;


    @PostMapping("/findServiceCommissionTier")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findServiceCommissionTier(@RequestBody ServiceCommissionTier request) {
        try {
            logInfo(request);
            return productCommissionTierService.find(request);
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/maintainServiceCommissionTier")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult maintainServiceCommissionTier(@RequestBody CommissionTierData request) {
        try {
            return productCommissionTierService.save(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();

        }
    }
}
