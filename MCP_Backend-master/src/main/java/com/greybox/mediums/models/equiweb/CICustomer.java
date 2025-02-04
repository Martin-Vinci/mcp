package com.greybox.mediums.models.equiweb;
import lombok.Data;

import java.util.List;

@Data
public class CICustomer {
    private String customerName;
    private String phoneNo;
    private String status;
    private String rimNo;
    private String entityType; // Customer, Outlet
    private String deviceID;
    private String outletCode;
    private Boolean lockedFlag;
    private String pinChangeFlag;
    private String firstPinGenerated;
    private List<CIAccount> accountList;
}
