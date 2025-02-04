package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import java.math.BigDecimal;
import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionBand {
    private BigDecimal transAmount;
    private Integer transCount;
    private String description;
    private Integer serviceCode;
    private Date startDate;
    private Date endDate;

}
