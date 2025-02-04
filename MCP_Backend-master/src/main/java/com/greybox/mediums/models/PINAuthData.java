package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PINAuthData {
    private String pinNo;
    private String phoneNo;
    private String deviceId;
    private String outletCode;
    private String channelCode;
}
