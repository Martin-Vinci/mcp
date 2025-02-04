package com.greybox.mediums.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CenteTrustExport {

    private String transId;
    private BigDecimal drAcctBal;
    private BigDecimal crAcctBal;
    private BigDecimal chargeAmt;
    private CenteTrustRespCode response;
}
