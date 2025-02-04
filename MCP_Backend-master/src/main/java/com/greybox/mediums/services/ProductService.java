package com.greybox.mediums.services;

import com.greybox.mediums.entities.ServiceRef;
import com.greybox.mediums.models.ErrorData;
import com.greybox.mediums.models.TransRequestData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.ServiceRefRepo;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.MediumException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ServiceRefRepo serviceRefRepo;

    public TxnResult find(ServiceRef request) {
        List<ServiceRef> customers = serviceRefRepo.findServices();
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public ServiceRef findServiceByCode(Integer serviceCode) throws MediumException {
        ServiceRef customers = serviceRefRepo.findServiceByCode(serviceCode);
        if (customers == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid transaction code specified").build());
        return customers;
    }

    public ServiceRef validateServiceConfiguration(TransRequestData request) throws MediumException {
        ServiceRef serviceRef = serviceRefRepo.findServiceByCode(request.getServiceCode());
        if (serviceRef == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid transaction code specified").build());
        if (!serviceRef.getStatus().equals("Active"))
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("This service is inactive at the moment").build());
        if (serviceRef.getMaxTransAmt() != null) {
            if (request.getTransAmt().compareTo(serviceRef.getMaxTransAmt()) > 0)
                throw new MediumException(ErrorData.builder()
                        .code("404")
                        .message("Transaction amount exceeds the maximum allowed configured amount [" + serviceRef.getMaxTransAmt() + "]").build());
        }
        if (serviceRef.getMinTransAmt() != null) {
            if (request.getTransAmt().compareTo(serviceRef.getMinTransAmt()) < 0)
                throw new MediumException(ErrorData.builder()
                        .code("404")
                        .message("Transaction amount is less than the minimum allowed configured amount [" + serviceRef.getMinTransAmt() + "]").build());
        }


        return serviceRef;
    }


    public TxnResult save(ServiceRef request) {
        request.setCreateDate(DataUtils.getCurrentDate().toLocalDate());
        serviceRefRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(ServiceRef request) {
        request.setCreateDate(DataUtils.getCurrentDate().toLocalDate());
        serviceRefRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }
}
