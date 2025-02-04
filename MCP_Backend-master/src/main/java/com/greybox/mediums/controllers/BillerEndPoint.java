package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.*;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.BillerNotifLogService;
import com.greybox.mediums.services.BillerNotifyService;
import com.greybox.mediums.services.BillerService;
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
@RequestMapping("/api/v1/biller")
@Api(tags = "Biller Control Services")
public class BillerEndPoint {
    @Autowired
    private BillerNotifyService billerNotifyService;
    @Autowired
    private BillerNotifLogService billerNotifLogService;
    @Autowired
    private BillerService billerService;

    @PostMapping("/findNotifications")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findNotifications(@RequestBody BillerNotif request) {
        try {
            logInfo(request);
            return billerNotifyService.find(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findBillerNotificationByReferenceNo")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findBillerNotificationByReferenceNo(@RequestBody BillerNotif request) {
        try {
            logInfo(request);
            return billerNotifyService.findBillerNotificationByReferenceNo(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }


    @PostMapping("/findNotificationLog")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findNotificationLog(@RequestBody BillerNotifLog request) {
        try {
            logInfo(request);
            return billerNotifLogService.find(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/maintainBiller")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult maintainBiller(@RequestBody Biller request) {
        try {
            return billerService.save(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findBillers")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findBillers(@RequestBody Biller request) {
        try {
            logInfo(request);
            return billerService.find(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findBillersByCode")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult findBillersByCode(@RequestBody Biller request) {
        try {
            logInfo(request);
            return billerService.findBillerByBillerCode(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findBillerProductsByBiller")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult findBillerProductsByBiller(@RequestBody BillerProduct request) {
        try {
            return billerService.findBillerProductsByBillerId(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findMobileBillerProductsByBiller")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult findMobileBillerProductsByBiller(@RequestBody BillerProduct request) {
        try {
            return billerService.findMobileBillerProductsByBiller(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }


    @PostMapping("/findBillerProductCategoryByBillerId")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult findBillerProductCategoryByBillerId(@RequestBody BillerProductCategory request) {
        try {
            return billerService.findBillerProductCategoryByBillerId(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findProductCategoryByBillerCode")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult findProductCategoryByBillerCode(@RequestBody Biller request) {
        try {
            return billerService.findProductCategoryByBillerCode(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }


    @PostMapping("/saveBillerProductCategory")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult saveBillerProductCategory(@RequestBody BillerProductCategory request) {
        try {
            return billerService.saveBillerProductCategory(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/saveBillerProduct")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult saveBillerProduct(@RequestBody BillerProduct request) {
        try {
            return billerService.saveBillerProduct(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }


}
