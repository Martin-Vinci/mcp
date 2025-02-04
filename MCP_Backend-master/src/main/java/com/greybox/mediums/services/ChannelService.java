package com.greybox.mediums.services;

import com.greybox.mediums.entities.Biller;
import com.greybox.mediums.entities.BillerProduct;
import com.greybox.mediums.entities.BillerProductCategory;
import com.greybox.mediums.entities.ServiceChannel;
import com.greybox.mediums.models.ErrorData;
import com.greybox.mediums.models.TransRequestData;
import com.greybox.mediums.models.TxnResult;
import com.greybox.mediums.repository.BillerProductCategoryRepo;
import com.greybox.mediums.repository.BillerProductRepo;
import com.greybox.mediums.repository.BillerRepo;
import com.greybox.mediums.repository.ServiceChannelRepo;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.MediumException;
import com.greybox.mediums.utils.PINDecryptor;
import lombok.var;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelService {

    @Autowired
    private ServiceChannelRepo serviceChannelRepo;

    public TxnResult find(ServiceChannel request) {
        List<ServiceChannel> customers = serviceChannelRepo.findServiceChannels();
        if (customers == null || customers.isEmpty())
            return TxnResult.builder().code("404")
                    .message("No records found")
                    .build();
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public void validateChannelConfiguration(String channelCode, String channelUsername, String channelPassword) {
        ServiceChannel serviceChannel = serviceChannelRepo.findServiceChannelByCode(channelCode);
        if (serviceChannel == null)
            throw new MediumException(ErrorData.builder()
                    .code("-99")
                    .message("Invalid Channel " + "[" + channelCode + "]" + " specified").build());
        if (serviceChannel.getChannelUsername() != null && serviceChannel.getChannelPassword() != null) {
            if (channelUsername == null || channelPassword == null)
                throw new MediumException(ErrorData.builder()
                        .code("-99").message("Channel User name and password is required").build());

            channelPassword = PINDecryptor.decrypt(channelPassword);
            System.out.println("============================== Client Channel Code " + channelCode);
            System.out.println("============================== Client Channel UserName " + channelUsername);
            System.out.println("============================== Client Channel Password " + channelPassword);

            System.out.println("============================== Save Channel Code " + serviceChannel.getChannelCode());
            System.out.println("============================== Save Channel UserName " + serviceChannel.getChannelUsername().trim());
            System.out.println("============================== Save Channel Password " + serviceChannel.getChannelPassword().trim());

            if (!channelUsername.equals(serviceChannel.getChannelUsername().trim()))
                throw new MediumException(ErrorData.builder()
                        .code("-99").message("Invalid Channel username or password specified").build());
            if (!channelPassword.equals(serviceChannel.getChannelPassword().trim()))
                throw new MediumException(ErrorData.builder()
                        .code("-99").message("Invalid Channel username or password specified").build());
            if (serviceChannel.getEnforcePwdExpiry().equals("Y")) {
                if (serviceChannel.getExpiryDate() != null) {
                    if (serviceChannel.getExpiryDate().isAfter(DataUtils.getCurrentDate().toLocalDate()))
                        throw new MediumException(ErrorData.builder()
                                .code("-99").message("Channel password is expired, Contact Micropay support").build());
                }
            }
        }
    }



    public TxnResult save(ServiceChannel request) {
        request.setCreateDt(DataUtils.getCurrentDate().toLocalDate());
        var validationResult = request.validateFields();
        // If validation fails, return the error message and code
        if (!validationResult.getCode().equals("00")) {
            return validationResult;
        }
        serviceChannelRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult update(ServiceChannel request) {
        request.setModifiedDate(DataUtils.getCurrentDate().toLocalDate());
        var validationResult = request.validateFields();
        // If validation fails, return the error message and code
        if (!validationResult.getCode().equals("00")) {
            return validationResult;
        }
        serviceChannelRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

}
