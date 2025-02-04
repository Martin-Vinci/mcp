package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.*;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.ChannelService;
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

import java.util.ArrayList;
import java.util.List;

import static com.greybox.mediums.utils.Logger.logError;
import static com.greybox.mediums.utils.Logger.logInfo;

@RestController
@RequestMapping("/api/v1/service-channel")
@Api(tags = "Channel Services")
public class ServiceChannelEndPoint {
    @Autowired
    private ChannelService channelService;

    @PostMapping("/maintainServiceChannel")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public TxnResult maintainServiceChannel(@RequestBody ServiceChannel request) {
        try {
            return channelService.save(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findChannels")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public TxnResult findChannels(@RequestBody ServiceChannel request) {
        try {
            return channelService.find(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }
}
