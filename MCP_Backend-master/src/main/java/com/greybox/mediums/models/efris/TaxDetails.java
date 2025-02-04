package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxDetails {
    private String taxCategoryCode;
    private String taxCategory;
    private BigDecimal netAmount;
    private String taxRate;
    private BigDecimal taxAmount;
    private BigDecimal grossAmount;
    private String exciseUnit;
    private String exciseCurrency;
    private String taxRateName;
}
