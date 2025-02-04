package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class TransReversalData extends OutletAuthRequest {
    private Long originTranId;
    private String reversalReason;
    public String apiUserName;
    public String apiPassword;
    public String deviceId;
    public String outletPhone;
    public String outletCode;
    public String pinNo;
    public String channelCode;
}