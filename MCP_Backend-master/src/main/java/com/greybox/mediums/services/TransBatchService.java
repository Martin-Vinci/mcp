package com.greybox.mediums.services;

import com.greybox.mediums.entities.MobileUser;
import com.greybox.mediums.entities.ServiceRef;
import com.greybox.mediums.entities.TransBatch;
import com.greybox.mediums.entities.TransBatchItem;
import com.greybox.mediums.models.ErrorData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.*;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.MediumException;
import com.greybox.mediums.utils.StringUtil;
import com.greybox.mediums.utils.UUIDGen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransBatchService {
    @Autowired
    private TransBatchRepo tfrBatchRepo;
    @Autowired
    private TransBatchItemRepo creditTfrItemRepo;
    @Autowired
    private ServiceRefRepo serviceRefRepo;
    @Autowired
    private MobileUserAccountRepo mobileUserRepo;
    @Autowired
    private ReportRepo reportRepo;


    public TxnResult findTransBatch(TransBatch request) {
        List<TransBatch> data = reportRepo.findTransBatchByPhone(request.getInitiatorPhone());
        if (data == null || data.isEmpty()) {
            return TxnResult.builder().message("No data found").code("404").build();
        }
        return TxnResult.builder().message("approved").
                code("00").data(data).build();
    }

    public TxnResult findTransBatchByUuId(TransBatch request) {
        TransBatch data = tfrBatchRepo.findByBatchUuid(request.getItemUuid());
        if (data == null)
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(data).build();
    }

    public TxnResult saveTransBatch(TransBatch request) {
        request.setCreateDate(DataUtils.getCurrentTimeStamp().toLocalDateTime());
        request.setItemUuid(UUIDGen.generateUuid());
        tfrBatchRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    private void validateBatchRequest(TransBatch request) {
        if (request.getServiceCode() == null)
            throw new MediumException(ErrorData.builder().code("-99").message("Service code is missing").build());
        if (request.getDrAcctNo() == null)
            throw new MediumException(ErrorData.builder().code("-99").message("Debtor account is missing").build());
    }

    public TxnResult updateTransBatch(TransBatch request) {
        TransBatch chequeBatch = tfrBatchRepo.getById(request.getBatchId());
        if (chequeBatch == null)
            return TxnResult.builder().code("404")
                    .message("Invalid batch Id specified")
                    .build();
        if (chequeBatch.getStatus().equalsIgnoreCase("Processed"))
            return TxnResult.builder().code("404")
                    .message("Sorry, Cannot update already Processed Batch")
                    .build();
        if (chequeBatch.getStatus().equalsIgnoreCase("SUBMITTED"))
            return TxnResult.builder().code("404")
                    .message("Cannot update Submitted Batch")
                    .build();

        validateBatchRequest(request);

        tfrBatchRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").build();
    }

    public TxnResult findTransBatchItemByUuId(TransBatch request) {
        List<TransBatchItem> data = creditTfrItemRepo.findByItemUuid(request.getItemUuid());
        if (data == null)
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(data).build();
    }

    public TxnResult findTransBatchItemById(TransBatchItem request) {
        TransBatchItem data = creditTfrItemRepo.getById(request.getBatchItemId());
        if (data == null)
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(data).build();
    }

    public TxnResult saveTransBatchItem(TransBatchItem request) {
        TransBatch transBatch = tfrBatchRepo.getById(request.getBatchId());
        if (transBatch == null)
            return TxnResult.builder().code("404").message("Invalid batch Id specified").build();

        request.setCreateDate(DataUtils.getCurrentTimeStamp().toLocalDateTime());
        creditTfrItemRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult updateTransBatchItem(TransBatchItem request) {

        TransBatch transBatch = tfrBatchRepo.getById(request.getBatchId());
        if (transBatch == null)
            return TxnResult.builder().code("404").message("Invalid batch Id specified").build();

        TransBatchItem item = creditTfrItemRepo.getById(request.getBatchItemId());
        if (item == null)
            return TxnResult.builder().code("404").message("Invalid batch Item Id specified").build();

        creditTfrItemRepo.save(item);
        return TxnResult.builder().message("approved").
                code("00").build();
    }

    public TxnResult createBatchItems(TransBatch request) {
        TransBatch creditTransferBatch = tfrBatchRepo.getById(request.getBatchId());
        if (creditTransferBatch == null)
            throw new MediumException(ErrorData.builder()
                    .code("-99").message("No match found for the specified Batch ID").build());

        ServiceRef serviceRef = serviceRefRepo.findServiceByCode(creditTransferBatch.getServiceCode());

        List<TransBatchItem> transBatchItems = request.getTransBatchItems();

        for (int i = 0; i < transBatchItems.size(); i++) {
            // Format the customer phone number
            String formattedPhoneNumber = StringUtil.formatPhoneNumber(transBatchItems.get(i).getCustomerPhoneNo());
            transBatchItems.get(i).setCustomerPhoneNo(formattedPhoneNumber);

            // Set batch ID and create date
            transBatchItems.get(i).setBatchId(creditTransferBatch.getBatchId());
            transBatchItems.get(i).setCreateDate(DataUtils.getCurrentTimeStamp().toLocalDateTime());
            transBatchItems.get(i).setStatus("Pending");

            // Process only if the transaction type is not "BILLS"
            if (!serviceRef.getTransType().trim().equalsIgnoreCase("BILL")) {
                // Find the account number associated with the customer phone number
                String customerAccount = this.mobileUserRepo.findAccountNumberByPhoneNumber(formattedPhoneNumber);

                // Check if account is not found and throw exception with the phone number in the message
                if (customerAccount == null || customerAccount.trim().isEmpty()) {
                    throw new MediumException(ErrorData.builder()
                            .code("-99")
                            .message("No match found for the specified phone number: " + formattedPhoneNumber)
                            .build());
                }
                // Set the customer account if found
                transBatchItems.get(i).setCustomerAccount(customerAccount);
            }
        }

        creditTfrItemRepo.saveAll(transBatchItems);
        return TxnResult.builder()
                .message("Approved")
                .code("00").build();
    }

    @Transactional
    public TxnResult updateTransBatchItem(List<TransBatchItem> request) {
        for (int i = 0; i < request.size(); i++) {
            creditTfrItemRepo.updateTransBatchItem(request.get(i).getStatus(), request.get(i).getMessage(), request.get(i).getBatchItemId());
        }
        return TxnResult.builder()
                .message("Approved")
                .code("00").build();
    }
}
