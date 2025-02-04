package com.greybox.mediums.models;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CashOutRequest extends OutletAuthRequest {
    private String customerPhone;
    private String customerAccount;
    private String withdrawOutletCode;
    private BigDecimal amount;
    private String withdrawCode;
    private Instant expiryDate;
}
