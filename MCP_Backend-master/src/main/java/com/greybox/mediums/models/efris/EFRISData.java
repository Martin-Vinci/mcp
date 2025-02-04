package com.greybox.mediums.models.efris;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EFRISData {
    public String content;
    public String signature;
    public EFRISDataDescr dataDescription;
}
