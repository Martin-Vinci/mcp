package com.greybox.mediums.models;

import com.greybox.mediums.entities.ServiceCommissionTier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommissionTierData {
    private ServiceCommissionTier[] data;
    private Integer commissionId;
    private String createdBy;
    private Date createDate;
}
