package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutletAuthRequest {
    @Transient
    private String apiUserName;
    @Transient
    private String apiPassword;
    @Transient
    private String deviceId;
    @Transient
    private String imeiNumber;
    @Transient
    private String deviceMake;
    @Transient
    private String newDeviceFlag;
    @Transient
    private String deviceModel;
    @Transient
    private String userPhoneNo;
    @Transient
    private String outletCode;
    @Transient
    private String pinNo;
    @Transient
    private String channelCode;
}