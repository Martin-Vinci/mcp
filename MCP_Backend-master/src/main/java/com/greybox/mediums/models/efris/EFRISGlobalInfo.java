package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EFRISGlobalInfo {
    private String appId;
    private String version;
    private String dataExchangeId;
    private String interfaceCode;
    private String requestCode;
    private String requestTime;
    private String responseCode;
    private String userName;
    private String deviceMAC;
    private String deviceNo;
    private String tin;
    private String brn;
    private String taxpayerID;
    private String longitude;
    private String latitude;
    public EFRISExtendField extendField;
}
