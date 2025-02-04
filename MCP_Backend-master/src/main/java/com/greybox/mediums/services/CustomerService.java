package com.greybox.mediums.services;

import com.greybox.mediums.entities.Account;
import com.greybox.mediums.entities.CustomerRef;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.AccountRepo;
import com.greybox.mediums.repository.CustomerRefRepo;
import com.greybox.mediums.utils.Encrypter;
import com.greybox.mediums.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRefRepo customerRefRepo;

    @Autowired
    private AccountRepo accountRepo;

    public TxnResult find(CustomerRef request) {
        List<CustomerRef> customers = customerRefRepo.findCustomers();
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult findAccountsByEntityId(Account request) {
        List<Account> customers = accountRepo.findAccountsByEntityId(request.getEntityId(), request.getEntityCode());
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    @Transactional
    public TxnResult save(CustomerRef request) throws NoSuchAlgorithmException {
        String pinString = StringUtil.generateRandomNumber(4) + "-" + request.getPhoneNo();
        DateFormat dateFormat = new SimpleDateFormat("yymmddhhmmss");
        request.setPinNo(Encrypter.encryptWithSHA256(pinString));
        CustomerRef response = customerRefRepo.save(request);
        String acctNo = request.getAcctNo() == null ? dateFormat.format(Calendar.getInstance().getTime()) : request.getAcctNo();
        Account account = new Account();
        account.setAcctNo(acctNo);
        account.setEntityCode("CUSTOMER");
        account.setEntityId(response.getCustomerId());
        account.setStatus("Active");
        account.setCreatedBy("SYSTEM");
        account.setCreateDt(request.getCreateDt());
        account.setLedgerBal(new BigDecimal(0));
        accountRepo.save(account);

        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(CustomerRef request) {
        customerRefRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
