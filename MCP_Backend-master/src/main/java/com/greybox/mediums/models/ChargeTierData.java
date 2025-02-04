package com.greybox.mediums.models;

import com.greybox.mediums.entities.ServiceChargeTier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargeTierData {
    private ServiceChargeTier[] data;
    private Integer chargeId;
    private String createdBy;
    private Date createDate;
}
