package com.greybox.mediums.models.equiweb;

import com.greybox.mediums.models.OutletAuthRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class CIStatementRequest extends OutletAuthRequest {
    private String accountNo;
    private String statementType;
    private String entityType;
    private Date fromDate;
    private Date toDate;
    private String transType;
}
