package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class AccountRequest extends OutletAuthRequest {
    private String phoneNo;
    private String acctNo;
    private String entityType;
    private String customerName;
}
