package com.greybox.mediums.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@Table(name = "service_commission_tiers", indexes = {@Index(name = "service_commission_tiers_commission_id_idx", columnList = "commission_id")})
@Entity
public class ServiceCommissionTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commission_tier_id", nullable = false)
    private Integer commissionTierId;

    @Column(name = "commission_id")
    private Integer commissionId;

    @Column(name = "tier_no")
    private Integer tierNo;

    @Column(name = "from_bal", nullable = false, precision = 21, scale = 2)
    private BigDecimal fromAmt;

    @Column(name = "to_bal", nullable = false, precision = 21, scale = 2)
    private BigDecimal toAmt;

    @Column(name = "amount", nullable = false, precision = 21, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "vendor_share", precision = 21, scale = 2)
    private BigDecimal vendorShare;

    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @Column(name = "created_by", nullable = false, length = 10)
    private String createdBy;

    @Column(name = "modify_date")
    private Date modifyDate;

    @Column(name = "modified_by", length = 10)
    private String modifiedBy;
}
