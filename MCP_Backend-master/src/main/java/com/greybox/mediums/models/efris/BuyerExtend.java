package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuyerExtend {
    private String propertyType;
    private String district;
    private String municipalityCounty;
    private String divisionSubcounty;
    private String town;
    private String cellVillage;
    private String effectiveRegistrationDate;
    private String meterStatus;
}
