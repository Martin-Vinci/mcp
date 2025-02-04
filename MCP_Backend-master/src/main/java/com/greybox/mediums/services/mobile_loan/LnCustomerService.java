package com.greybox.mediums.services.mobile_loan;

import com.greybox.mediums.config.SchemaConfig;
import com.greybox.mediums.entities.MessageOutbox;
import com.greybox.mediums.entities.mobile_loan.LnCustomer;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.AuthenticationRepoImpl;
import com.greybox.mediums.repository.UserRepo;
import com.greybox.mediums.repository.mobile_loan.CustomerRepo;
import com.greybox.mediums.security.Encryptor;
import com.greybox.mediums.services.MessageService;
import com.greybox.mediums.utils.Encrypter;
import com.greybox.mediums.utils.MediumException;
import com.greybox.mediums.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class LnCustomerService {

    @Autowired
    private CustomerRepo customerRepo;
    @Autowired
    private UserRepo repo;
    @Autowired
    private SchemaConfig schemaConfig;
    @Autowired
    private AuthenticationRepoImpl pwdRepo;
    @Autowired
    Encryptor encryptor;

    @Autowired
    private MessageService messageService;
    
    public TxnResult findAll(LnCustomer request) {
        List<LnCustomer> charges = customerRepo.findAll();
        if (charges == null || charges.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(charges).build();
    }

    @Transactional
    public TxnResult authentication(LnCustomer request) throws Exception {

        request.setPhoneNo(StringUtil.formatPhoneNumber(request.getPhoneNo()));
        String pinString = request.getPassword() + "-" + request.getPhoneNo();

        String encryptedRequestPassword = Encrypter.encryptWithSHA256(pinString);
        System.out.println("======================Encrypted Password: " + encryptedRequestPassword);
        System.out.println("======================Phone Number: " + request.getPhoneNo());
        LnCustomer data = customerRepo.findCustomerByPhoneAndPin(request.getPhoneNo(), encryptedRequestPassword.trim());
        if (data == null) {
            return TxnResult.builder().message("Unauthorized").
                    code("403").build();
        }

        return TxnResult.builder().message("approved").
                code("00").data(data).build();
    }

    public TxnResult findCustById(LnCustomer request) {
        LnCustomer customer = customerRepo.findCustById(request.getId());
        if (customer == null)
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customer).build();
    }

    public TxnResult save(LnCustomer request) throws MediumException, NoSuchAlgorithmException, URISyntaxException {
        LnCustomer data = customerRepo.findCustomerByPhone(request.getPhoneNo());
        if (data != null){
            return TxnResult.builder().message("Phone number is already registered for Webcash").
                    code("403").build();
        }

        request.setPhoneNo(StringUtil.formatPhoneNumber(request.getPhoneNo()));
        String plainPin = StringUtil.generateRandomNumber(4);
        String pinString = plainPin + "-" + request.getPhoneNo();
        String encryptedPin = Encrypter.encryptWithSHA256(pinString);

        MessageOutbox messageOutbox = new MessageOutbox();
        String messageText = "Webcash: Your account PIN is " + plainPin;
        messageOutbox.setRecipientNumber(request.getPhoneNo());
        messageOutbox.setMessageText(messageText);
        messageOutbox.setDeliverSMS(true);
        request.setPassword(encryptedPin);
        customerRepo.save(request);
        messageService.logSMS(messageOutbox);

        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(LnCustomer request) {
        customerRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
