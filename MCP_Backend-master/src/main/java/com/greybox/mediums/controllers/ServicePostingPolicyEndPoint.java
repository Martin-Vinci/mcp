package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.ServicePostingDetail;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.PostingPolicyService;
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

@RestController
@RequestMapping({"/api/v1/agent-banking/service"})
@Api(tags = {"Product Charge Services"})
public class ServicePostingPolicyEndPoint {

    @Autowired
    private PostingPolicyService postingPolicyService;

    @PostMapping({"/findPostingPolicyAccountTypes"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findPostingPolicyAccountTypes(@RequestBody ServicePostingDetail request) {
        try {
            Logger.logInfo(request);
            return this.postingPolicyService.findPostingPolicyAccountTypes();
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findPostingPolicyAmountTypes"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findPostingPolicyAmountTypes(@RequestBody ServicePostingDetail request) {
        try {
            Logger.logInfo(request);
            return this.postingPolicyService.findPostingPolicyAmountTypes();
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findServicePostingPolicy"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    public TxnResult findServicePostingPolicy(@RequestBody ServicePostingDetail request) {
        try {
            Logger.logInfo(request);
            return this.postingPolicyService.find(request.getServiceId());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/deleteServicePostingDetail"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    public TxnResult deleteServicePostingDetail(@RequestBody ServicePostingDetail request) {
        try {
            Logger.logInfo(request);
            return this.postingPolicyService.deleteServicePostingDetail(request);
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/maintainServicePostingPolicy"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult maintainServicePostingPolicy(@RequestBody ServicePostingDetail request) {
        try {
            return this.postingPolicyService.save(request);
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }
}
