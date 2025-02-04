package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CenteTrustAmountType {
    private Integer serviceCode;
    private String amountType;
}