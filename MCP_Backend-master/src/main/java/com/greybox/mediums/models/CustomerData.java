package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerData {
    private String customerName;
    private String effectiveDate;
    private String phoneNo;
    private String status;
    private String rimNo;
    private String entityType; // Customer, Outlet
    private String deviceID;
    private String outletCode;
    private boolean lockedFlag;
    private boolean pinChangeFlag;
    private List<AccountData> accountList;
}
