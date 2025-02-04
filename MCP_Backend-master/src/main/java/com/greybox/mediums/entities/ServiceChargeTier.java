package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "service_charge_tiers", indexes = {
        @Index(name = "service_charge_tiers_charge_id_idx", columnList = "charge_id")
})
@Entity
@Data
public class ServiceChargeTier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "charge_tier_id", nullable = false)
    private Integer chargeTierId;

    @Column(name = "charge_id")
    private Integer chargeId;

    @Column(name = "tier_no")
    private Integer tierNo;

    @Column(name = "from_bal", nullable = false, precision = 21, scale = 2)
    private BigDecimal fromAmt;

    @Column(name = "to_bal", nullable = false, precision = 21, scale = 2)
    private BigDecimal toAmt;

    @Column(name = "amount", nullable = false, precision = 21, scale = 2)
    private BigDecimal chargeAmt;

    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @Column(name = "created_by", nullable = false, length = 10)
    private String createdBy;

    @Column(name = "modify_date")
    private Date modifyDate;

    @Column(name = "modified_by", length = 10)
    private String modifiedBy;

}