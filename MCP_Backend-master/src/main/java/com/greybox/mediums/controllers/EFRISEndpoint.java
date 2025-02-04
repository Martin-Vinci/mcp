package com.greybox.mediums.controllers;


import com.greybox.mediums.entities.EfrisCommodity;
import com.greybox.mediums.entities.EfrisInvoice;
import com.greybox.mediums.models.efris.CommodityPage;
import com.greybox.mediums.models.efris.GoodsRecord;
import com.greybox.mediums.models.efris.InvoiceData;
import com.greybox.mediums.models.efris.TaxPayer;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.ura.URAInvoiceService;
import com.greybox.mediums.utils.CommonResponse;
import com.greybox.mediums.utils.FileDownloadUtility;
import com.greybox.mediums.utils.MediumException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.greybox.mediums.utils.Logger.logError;
import static com.greybox.mediums.utils.Logger.logInfo;

@RestController
@RequestMapping("/api/v1/efris")
@Api(tags = "EFRIS Service")
public class EFRISEndpoint {

    @Autowired
    private URAInvoiceService invoiceService;

    @PostMapping("/queryTaxPayerInformation")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult queryTaxPayerInformation(@RequestBody TaxPayer request) {
        try {
            logInfo(request);
            return invoiceService.queryTaxPayerInformation(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            if (e.getMessage().contains("timeout"))
                return TxnResult.builder().message("Timeout error has occurred while waiting for response from URA").
                        code("-99").build();
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/querySystemDictionaryUpdate")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult querySystemDictionaryUpdate(@RequestBody TaxPayer request) {
        try {
            logInfo(request);
            return invoiceService.querySystemDictionaryUpdate();
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            if (e.getMessage().contains("timeout"))
                return TxnResult.builder().message("Timeout error has occurred while waiting for response from URA").
                        code("-99").build();
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/extractCommoditiesFromURAPortal")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult extractCommoditiesFromURAPortal(@RequestBody CommodityPage request) {
        try {
            logInfo(request);
            return invoiceService.extractCommoditiesFromURAPortal(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            if (e.getMessage().contains("timeout"))
                return TxnResult.builder().message("Timeout error has occurred while waiting for response from URA").
                        code("-99").build();
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/queryAllCommodityCategory")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult queryAllCommodityCategory(@RequestBody TaxPayer request) {
        try {
            logInfo(request);
            return invoiceService.queryAllCommodityCategory();
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            if (e.getMessage().contains("timeout"))
                return TxnResult.builder().message("Timeout error has occurred while waiting for response from URA").
                        code("-99").build();
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }


    @PostMapping("/queryCommodityCategoryByParentCode")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Retrieves all customer addresses in the system")
    public TxnResult queryCommodityCategoryByParentCode(@RequestBody EfrisCommodity request) {
        try {
            logInfo(request);
            return invoiceService.queryCommodityCategory(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            if (e.getMessage().contains("timeout"))
                return TxnResult.builder().message("Timeout error has occurred while waiting for response from URA").
                        code("-99").build();
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/createInvoice")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult createInvoice(@RequestBody InvoiceData request) {
        try {
            logInfo(request);
            return invoiceService.createInvoice(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            if (e.getMessage().contains("timeout"))
                return TxnResult.builder().message("Timeout error has occurred while waiting for response from URA").
                        code("-99").build();
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/goodsUpload")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult goodsUpload(@RequestBody GoodsRecord request) {
        try {
            logInfo(request);
            return invoiceService.goodsUpload(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            if (e.getMessage().contains("timeout"))
                return TxnResult.builder().message("Timeout error has occurred while waiting for response from URA").
                        code("-99").build();
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/goodsAndServiceInquiry")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult goodsAndServiceInquiry(@RequestBody GoodsRecord request) {
        try {
            logInfo(request);
            return invoiceService.goodsAndServiceInquiry(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            if (e.getMessage().contains("timeout"))
                return TxnResult.builder().message("Timeout error has occurred while waiting for response from URA").
                        code("-99").build();
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }


    @PostMapping("/findAllInvoices")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @Operation(summary = "Creates a customer address in the system")
    public TxnResult findAllInvoices(@RequestBody EfrisInvoice request) {
        try {
            logInfo(request);
            return invoiceService.findAllInvoices(request);
        } catch (MediumException e1) {
            logError(e1);
            return CommonResponse.getMediumExceptionError(e1.getErrorMessage());
        } catch (Exception e) {
            if (e.getMessage().contains("timeout"))
                return TxnResult.builder().message("Timeout error has occurred while waiting for response from URA").
                        code("-99").build();
            logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @GetMapping("/invoiceDownLoad/{fileCode}")
    public ResponseEntity<?> invoiceDownLoad(@PathVariable("fileCode") String invoiceNo) {
        FileDownloadUtility downloadUtil = new FileDownloadUtility();
        Resource resource = null;
        try {
            resource = downloadUtil.getFileAsResource(invoiceNo);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
        if (resource == null) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }
        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }


}
