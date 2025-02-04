package com.greybox.mediums.models.efris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EFRISDataDescr {
    private String codeType;
    private String encryptCode;
    private String zipCode;
}
