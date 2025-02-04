package com.greybox.mediums.services;

import com.greybox.mediums.entities.MessageOutbox;
import com.greybox.mediums.models.SearchCriteria;
import com.greybox.mediums.models.SpeedaSMSResp;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.MessageOutboxRepo;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.Logger;
import com.greybox.mediums.utils.MediumException;
import com.greybox.mediums.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageOutboxRepo messageOutboxRepo;

    public TxnResult findSMSByDateRange(SearchCriteria request) throws MediumException {
        List<MessageOutbox> customers;
        if (request.getPhoneNo() != null) {
            request.setPhoneNo(StringUtil.formatPhoneNumber(request.getPhoneNo()));
            customers = messageOutboxRepo.findSMSByPhone(request.getFromDate(), request.getToDate(), request.getPhoneNo());
        } else
            customers = messageOutboxRepo.findSMSByDateRange(request.getFromDate(), request.getToDate());
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult findRecipientSMS(String phoneNo) {
        List<MessageOutbox> customers = messageOutboxRepo.findMessages(phoneNo);
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();

        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public void logSMS(MessageOutbox request) {
        RestTemplate restTemplate = new RestTemplate();
        MessageOutbox resp = null;
        try {
            Logger.logInfo("Starting SMS log for recipient: " + request.getRecipientNumber());
            request.setRecipientNumber(StringUtil.formatPhoneNumber(request.getRecipientNumber()));
            request.setEmailMessage(false);
            request.setFlashMessage(false);
            request.setMessageStatus("NOT_SENT");
            request.setTimeGenerated(DataUtils.getCurrentTimeStamp().toLocalDateTime());
            resp = messageOutboxRepo.save(request);
            Logger.logInfo("MessageOutbox saved: {} => " +  resp);

            if (!request.isDeliverSMS()) {
                Logger.logInfo("SMS delivery is disabled for recipient: {} =>" + request.getRecipientNumber());
                return;
            }

            final String baseUrl = "http://apidocs.speedamobile.com/api/SendSMS";
            URI uri = new URI(baseUrl);
            HashMap<String, Object> requestData = new HashMap<>();
            requestData.put("api_id", "API86997544689");
            requestData.put("api_password", "s$FTq66KwBn2NjQs");
            requestData.put("sms_type", "T");
            requestData.put("encoding", "T");
            requestData.put("sender_id", "MICROPAY");
            requestData.put("phonenumber", request.getRecipientNumber());
            requestData.put("templateid", null);
            requestData.put("textmessage", request.getMessageText());

            Logger.logInfo("Sending SMS with request data: {} => " +  requestData);
            ResponseEntity<SpeedaSMSResp> result = restTemplate.postForEntity(uri, requestData, SpeedaSMSResp.class);
            Logger.logInfo("Received response from SMS API: {} => " + result.getBody());

            // Verify request succeeded
            request.setMessageStatus("SENT");
            request.setMessageId(resp.getMessageId());
            request.setTimeSent(DataUtils.getCurrentTimeStamp().toLocalDateTime());
            messageOutboxRepo.save(request);
            Logger.logInfo("SMS successfully sent to recipient: {} => " +  request.getRecipientNumber());
        } catch (Exception e) {
            if (resp != null)
                request.setMessageId(resp.getMessageId());
            request.setFailureReason(e.getMessage());
            try {
                messageOutboxRepo.save(request);
            }catch (Exception ex) {

            }
            Logger.logInfo("Failed to send SMS to recipient: {}. Error: {} => Request Object" + request + "Error => " + e);
        }
    }

}
