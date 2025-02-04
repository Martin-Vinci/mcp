package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.AgentCompanyInfo;
import com.greybox.mediums.entities.MobileUser;
import com.greybox.mediums.entities.MobileUserAccount;
import com.greybox.mediums.models.OutletAuthRequest;
import com.greybox.mediums.models.SearchCriteria;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.MessageService;
import com.greybox.mediums.services.MobileUserService;
import com.greybox.mediums.utils.CommonResponse;
import com.greybox.mediums.utils.Logger;
import com.greybox.mediums.utils.MediumException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.v3.oas.annotations.Operation;
import java.beans.PropertyEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/agent-banking/mobileUser"})
@Api(tags = {"Mobile Services"})
public class MobileEndPoint {

    @Autowired
    private MobileUserService mobileUserService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        StringTrimmerEditor stringtrimmer = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, (PropertyEditor)stringtrimmer);
    }

    @Autowired
    private MessageService messageService;

    @PostMapping({"/findAgentCompanyInfo"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    public TxnResult findAgentCompanyInfo(@RequestBody AgentCompanyInfo request) {
        try {
            Logger.logInfo(request);
            return this.mobileUserService.findAgentCompanyInfo(request);
        } catch (MediumException e1) {
            Logger.logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findMobileUser"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findCustomer(@RequestBody MobileUser request) {
        try {
            Logger.logInfo(request);
            return this.mobileUserService.find(request);
        } catch (MediumException e1) {
            Logger.logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findAgents"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findAgents(@RequestBody MobileUser request) {
        try {
            Logger.logInfo(request);
            return this.mobileUserService.findAgents(request);
        } catch (MediumException e1) {
            Logger.logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findOutlets"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findOutlets(@RequestBody MobileUser request) {
        try {
            Logger.logInfo(request);
            return this.mobileUserService.findOutlets(request);
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findTransactingAgents"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findTransactingAgents(@RequestBody MobileUser request) {
        try {
            Logger.logInfo(request);
            return this.mobileUserService.findTransactingAgents(request);
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findAllSMS"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findAllSMS(@RequestBody SearchCriteria request) {
        try {
            Logger.logInfo(request);
            return this.messageService.findSMSByDateRange(request);
        } catch (MediumException e1) {
            Logger.logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findMobileUserAccounts"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findMobileUserAccounts(@RequestBody MobileUserAccount request) {
        try {
            Logger.logInfo(request);
            return this.mobileUserService.find(request);
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/maintainMobileUser"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult maintainCustomer(@RequestBody MobileUser request) {
        try {
            return this.mobileUserService.enrollCustomerByAgent(request);
        } catch (MediumException e1) {
            Logger.logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/pinReset"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult pinReset(@RequestBody MobileUser request) {
        try {
            return this.mobileUserService.pinReset(request);
        } catch (MediumException e1) {
            Logger.logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findPendingCustomers"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findPendingCustomers(@RequestBody MobileUser request) {
        try {
            Logger.logInfo(request);
            return this.mobileUserService.findPendingCustomers(request);
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/reviewMobileUser"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult reviewMobileUser(@RequestBody MobileUser request) {
        try {
            return this.mobileUserService.reviewMobileUser(request);
        } catch (MediumException e1) {
            Logger.logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/findCustomerDetails"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findCustomerDetails(@RequestBody MobileUser request) {
        try {
            Logger.logInfo(request);
            return this.mobileUserService.findCustomerDetails(request);
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/pinAuthentication"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult pinAuthentication(@RequestBody OutletAuthRequest request) {
        try {
            return this.mobileUserService.pinAuthentication(request);
        } catch (MediumException e1) {
            Logger.logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/updateMobileCustomer"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult updateMobileCustomer(@RequestBody MobileUser request) {
        try {
            return this.mobileUserService.registerCustomer(request);
        } catch (MediumException e1) {
            Logger.logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            if (e.getMessage().contains("mobile_users_phone_number_idx")) {
                return TxnResult.builder()
                        .code("-99")
                        .message("Specified phone number is already registered in the system").build();
            }
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/maintainAgent"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult maintainAgent(@RequestBody MobileUser request) {
        try {
            return this.mobileUserService.maintainAgent(request);
        } catch (MediumException e1) {
            Logger.logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            if (e.getMessage().contains("mobile_users_phone_number_idx")) {
                return TxnResult.builder()
                        .code("-99")
                        .message("Specified phone number is already registered in the system").build();
            }
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping({"/saveUserAccount"})
    @ApiOperation(value = "", authorizations = {@Authorization("apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult saveUserAccount(@RequestBody MobileUserAccount request) {
        try {
            return this.mobileUserService.saveUserAccount(request);
        } catch (MediumException e1) {
            Logger.logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

}
