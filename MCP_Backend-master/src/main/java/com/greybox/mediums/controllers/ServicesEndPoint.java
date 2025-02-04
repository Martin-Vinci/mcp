package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.ServiceRef;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.ProductService;
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
@RequestMapping("/api/v1/agent-banking/services")
@Api(tags = "Product Services")
public class ServicesEndPoint {

    @Autowired
    private ProductService productService;

    @PostMapping("/findServices")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findServices(@RequestBody ServiceRef request) {
        try {
            logInfo(request);
            return productService.find(request);
        }catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/maintainServices")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult maintainServices(@RequestBody ServiceRef request) {
        try {
            if (request.getEdit())
                return productService.update(request);
            else
                return productService.save(request);
        }catch (Exception e){
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }
}
