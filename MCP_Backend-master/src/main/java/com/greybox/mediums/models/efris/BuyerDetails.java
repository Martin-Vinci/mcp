package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuyerDetails {
    private String buyerTin;
    private String buyerNinBrn;
    private String buyerPassportNum;
    private String buyerLegalName;
    private String buyerBusinessName;
    private String buyerAddress;
    private String buyerEmail;
    private String buyerMobilePhone;
    private String buyerLinePhone;
    private String buyerPlaceOfBusi;
    private String buyerType;
    private String buyerCitizenship;
    private String buyerSector;
    private String buyerReferenceNo;
}
