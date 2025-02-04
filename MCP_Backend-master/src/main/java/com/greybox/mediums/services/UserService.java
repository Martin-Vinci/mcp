package com.greybox.mediums.services;

import com.greybox.mediums.entities.User;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.UserRepo;
import com.greybox.mediums.security.Encryptor;
import com.greybox.mediums.utils.DataUtils;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    Encryptor encryptor;

    @Autowired
    private UserRepo userRepo;

    public TxnResult find(User request) {
        List<User> customers;
        if (request.getFullName() != null)
            customers = userRepo.findUsersByName(request.getFullName());
        else
            customers = userRepo.findUsers();
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult save(User request) throws Exception {
        request.setCreateDt(DataUtils.getCurrentDate().toLocalDate());
        request.setUserPwd(encryptor.encrypt(request.getUserPwd().trim()));
        request.setPwdEnhancedFlag("N");
        request.setStatus("Active");
        userRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(User request) {
        request.setModifyDt(DataUtils.getCurrentDate().toLocalDate());
        userRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
