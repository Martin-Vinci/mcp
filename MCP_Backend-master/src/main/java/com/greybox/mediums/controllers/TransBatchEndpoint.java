package com.greybox.mediums.controllers;

import com.greybox.mediums.entities.TransBatch;
import com.greybox.mediums.entities.TransBatchItem;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.services.TransBatchService;
import com.greybox.mediums.utils.CommonResponse;
import com.greybox.mediums.utils.Logger;
import com.greybox.mediums.utils.MediumException;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/batch-service")
@Api(tags = "Batch Service")
public class TransBatchEndpoint {

    @Autowired
    private TransBatchService service;

    @PostMapping("/findTransBatch")
    public TxnResult findTransBatch(@RequestBody TransBatch request) {
        try {
            Logger.logInfo(request);
            return service.findTransBatch(request);
        } catch (MediumException e2) {
            Logger.logError(e2);
            return CommonResponse.getMediumExceptionError(e2.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findTransBatchByUuId")
    public TxnResult findTransBatchByUuId(@RequestBody TransBatch request) {
        try {
            Logger.logInfo(request);
            return service.findTransBatchByUuId(request);
        } catch (MediumException e2) {
            Logger.logError(e2);
            return CommonResponse.getMediumExceptionError(e2.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/maintainTransBatch")
    public TxnResult maintainTransBatch(@RequestBody TransBatch request) {
        try {
            if (request.getBatchId() == null)
                return service.saveTransBatch(request);
            else
                return service.updateTransBatch(request);
        } catch (MediumException e2) {
            Logger.logError(e2);
            return CommonResponse.getMediumExceptionError(e2.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findTransBatchItems")
    public TxnResult findTransBatchItems(@RequestBody TransBatch request) {
        try {
            Logger.logInfo(request);
            return service.findTransBatchItemByUuId(request);
        } catch (MediumException e2) {
            Logger.logError(e2);
            return CommonResponse.getMediumExceptionError(e2.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/findTransBatchItemById")
    public TxnResult findTransBatchItemById(@RequestBody TransBatchItem request) {
        try {
            Logger.logInfo(request);
            return service.findTransBatchItemById(request);
        } catch (MediumException e2) {
            Logger.logError(e2);
            return CommonResponse.getMediumExceptionError(e2.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }


    @PostMapping("/maintainTransBatchItem")
    public TxnResult maintainTransBatchItem(@RequestBody TransBatchItem request) {
        try {
            Logger.logInfo(request);

            if (request.getBatchId() == null)
                return service.saveTransBatchItem(request);
            else
                return service.updateTransBatchItem(request);
        } catch (MediumException e2) {
            Logger.logError(e2);
            return CommonResponse.getMediumExceptionError(e2.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/createBatchItems")
    public TxnResult createBatchItems(@RequestBody TransBatch request) {
        try {
            Logger.logInfo(request);
            return service.createBatchItems(request);
        } catch (MediumException e2) {
            Logger.logError(e2);
            return CommonResponse.getMediumExceptionError(e2.getErrorMessage());
        } catch (Exception e1) {
            Logger.logError(e1);
            return CommonResponse.getUndefinedError();
        }
    }

    @PostMapping("/updateTransBatchItem")
    public TxnResult updateTransBatchItem(@RequestBody List<TransBatchItem> request) {
        try {
            Logger.logInfo(request);
            return service.updateTransBatchItem(request);
        } catch (MediumException e2) {
            Logger.logError(e2);
            return CommonResponse.getMediumExceptionError(e2.getErrorMessage());
        } catch (Exception e) {
            Logger.logError(e);
            return CommonResponse.getUndefinedError();
        }
    }
}