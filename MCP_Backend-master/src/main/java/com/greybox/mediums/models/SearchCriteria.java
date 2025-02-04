package com.greybox.mediums.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchCriteria {
    private String scope;
    private String categories;
    private String phoneNo;
    private Date fromDate;
    private Date toDate;
}
