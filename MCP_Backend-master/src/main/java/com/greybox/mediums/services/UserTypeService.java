package com.greybox.mediums.services;

import com.greybox.mediums.entities.UserType;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.UserTypeRepo;
import com.greybox.mediums.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserTypeService {

    @Autowired
    private UserTypeRepo memberTypeRepo;


    public TxnResult find(UserType request) {
        List<UserType> customers = memberTypeRepo.findUserTypes();
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public TxnResult save(UserType request) {
        request.setCreateDt(DataUtils.getCurrentDate().toLocalDate());
        memberTypeRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(UserType request) {
        request.setModifyDt(DataUtils.getCurrentDate().toLocalDate());
        memberTypeRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
