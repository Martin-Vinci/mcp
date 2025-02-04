package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerDetails {
    private String tin;
    private String ninBrn;
    private String legalName;
    private String businessName;
    private String address;
    private String mobilePhone;
    private String linePhone;
    private String emailAddress;
    private String placeOfBusiness;
    private String referenceNo;
    private String branchId;
    private String isCheckReferenceNo;
}
