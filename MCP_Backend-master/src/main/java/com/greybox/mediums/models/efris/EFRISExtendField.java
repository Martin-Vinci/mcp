package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EFRISExtendField {
    private String responseDateFormat;
    private String responseTimeFormat;
    private String referenceNo;
}
