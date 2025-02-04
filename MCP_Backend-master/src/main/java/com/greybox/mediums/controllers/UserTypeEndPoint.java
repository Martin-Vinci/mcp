package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.AccessMenuRight;
import com.greybox.mediums.entities.User;
import com.greybox.mediums.entities.UserType;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.AccessMenuRightService;
import com.greybox.mediums.services.UserRoleService;
import com.greybox.mediums.services.UserService;
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
@RequestMapping("/api/v1/system-admin/user-role")
@Api(tags = "UserType Services")
public class UserTypeEndPoint {
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private AccessMenuRightService accessMenuRightService;

    @PostMapping("/findUserRole")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public TxnResult findUserRole(@RequestBody UserType request) {
        try {
            logInfo(request);
            return userRoleService.find(request);
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/maintainUserRole")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult maintainUserRole(@RequestBody UserType request) {
        try {
            if (request.getUserTypeId() != null)
                return userRoleService.update(request);
            else
                return userRoleService.save(request);
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }


    @PostMapping("/assignUserRoleAccessRight")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult assignUserRoleAccessRight(@RequestBody AccessMenuRight[] request) {
        try {
            logInfo(request);
            return accessMenuRightService.assignAccessRight(request);
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/revokeUserRoleAccessRight")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult revokeAccessRight(@RequestBody AccessMenuRight[] request) {
        try {
            logInfo(request);
            return accessMenuRightService.revokeAccessRight(request);
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findUserRoleAssignedAccessRights")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult findAssignedAccessRights(@RequestBody AccessMenuRight request) {
        try {
            return accessMenuRightService.findAssignedAccessMenu(request.getUserTypeId());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findUnAssignedUserRoleAccessRights")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult findUnAssignedAccessRights(@RequestBody AccessMenuRight request) {
        try {
            return accessMenuRightService.findUnAssignedAccessMenu(request);
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }
}
