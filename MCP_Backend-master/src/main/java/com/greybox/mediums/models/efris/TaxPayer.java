package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxPayer {
    public String tin;
    public String ninBrn;
    public String legalName;
    public String businessName;
    public String contactNumber;
    public String contactEmail;
    public String address;
    public String taxpayerType;
}
