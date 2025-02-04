package com.greybox.mediums.controllers;

import com.greybox.mediums.models.AuthRequest;
import com.greybox.mediums.models.PasswordChangeRequest;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.security.AuthenticationService;
import com.greybox.mediums.utils.CommonResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import static com.greybox.mediums.utils.Logger.logError;

@RestController
@RequestMapping("/api/v1/security")
@Api(tags = "Authentication services")
public class AuthenticationEndpoint {

    @Autowired
    private AuthenticationService service;

    @PostMapping("/loginUser")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Manages user authentication in the system")
    public TxnResult authentication(@RequestBody AuthRequest request) {
        try {
//            AuthResponse data = new AuthResponse();
//            data.setUserName("GP0001");
//            data.setEmployeeId(1);
//            data.setFullName("Gamwanga");
//            data.setProcessDate("2021-12-12");
//            data.setStatus("A");
//            data.setPwdEnhancedFlag("Y");
//            if (1 == 1)
//                return TxnResult.builder().code("00").data(data).build();


            return service.authentication(request);
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/changePassword")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Manages user authentication in the system")
    public TxnResult changePassword(@RequestBody PasswordChangeRequest request) {
        try {
            return service.changePassword(request);
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/resetUserPassword")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Manages user authentication in the system")
    public TxnResult resetUserPassword(@RequestBody PasswordChangeRequest request) {
        try {
            return service.resetUserPassword(request);
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }


    @PostMapping("/logoutUser")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Manages user authentication in the system")
    public TxnResult logoutUser(@RequestBody AuthRequest request) {
        try {
            return service.logoutUser(request);
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @GetMapping("getMenus/{userTypeId}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public TxnResult getMenus(@PathVariable("userTypeId") Integer userTypeId) {
        try {
            return service.getMenus(userTypeId);
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }


}
