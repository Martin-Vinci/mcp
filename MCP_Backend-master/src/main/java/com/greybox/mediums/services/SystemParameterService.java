package com.greybox.mediums.services;

import com.greybox.mediums.entities.ServiceRef;
import com.greybox.mediums.entities.SystemParameter;
import com.greybox.mediums.models.ErrorData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.SystemParameterRepo;
import com.greybox.mediums.utils.MediumException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemParameterService {

    @Autowired
    private SystemParameterRepo userRepo;
    private final Map<String, SystemParameter> systemParameterMap = new HashMap<>(); // In-memory cache

    // This method will be called at system startup to load all users
    @PostConstruct
    public void loadParametersIntoMemory() {
        List<SystemParameter> systemParameterList = userRepo.findParameters();
        for (SystemParameter user : systemParameterList) {
            systemParameterMap.put(user.getParamCode(), user);
        }
    }
    // Method to get user by username
    public SystemParameter getParameter(String userName) {
        return systemParameterMap.get(userName);
    }

    public TxnResult find(SystemParameter request) {
        List<SystemParameter> customers = userRepo.findParameters();
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public String findParameterByCode(String parameterCode) throws MediumException {
        SystemParameter systemParameter = userRepo.findParameterByCode(parameterCode);
        if (systemParameter == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("No matches found for the specified parameter code").build());
        return systemParameter.getParamValue();
    }

    public TxnResult save(SystemParameter request) {
        userRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(SystemParameter request) {
        userRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
