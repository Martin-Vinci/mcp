package com.greybox.mediums.utils;

import com.greybox.mediums.models.OutletAuthRequest;
import org.springframework.stereotype.Service;

@Service
public class CommonService {

    public static OutletAuthRequest getCommonRequest(
            OutletAuthRequest requestData,
            String apiUserName,
            String deviceId,
            String outletPhone,
            String outletCode,
            String pinNo,
            String channelCode
    ) {
        requestData.setChannelCode(channelCode);
        requestData.setDeviceId(deviceId);
        requestData.setOutletCode(outletCode);
        requestData.setUserPhoneNo(outletPhone);
        requestData.setPinNo(pinNo);
        return requestData;
    }
}

