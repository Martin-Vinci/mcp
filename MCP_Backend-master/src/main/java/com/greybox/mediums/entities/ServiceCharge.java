package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Table(name = "service_charge", indexes = {
        @Index(name = "service_charge_charge_type_idx", columnList = "charge_type"),
        @Index(name = "service_charge_status_idx", columnList = "status"),
        @Index(name = "service_charge_service_id_idx", columnList = "service_id")
})
@Entity
@Data
public class ServiceCharge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "charge_id", nullable = false)
    private Integer chargeId;

    @Column(name = "service_id", nullable = false)
    private Integer serviceId;

    @Column(name = "charge_type", nullable = false, length = 7)
    private String chargeType;

    @Column(name = "amount", precision = 21, scale = 2)
    private BigDecimal amount;

    @Column(name = "calculation_basis", length = 20)
    private String calculationBasis;

    @Column(name = "status", length = 10)
    private String status;

    @Column(name = "create_date")
    private LocalDate createDate;

    @Column(name = "created_by", length = 10)
    private String createdBy;

    @Column(name = "modify_date")
    private LocalDate modifyDate;

    @Column(name = "modified_by", length = 10)
    private String modifiedBy;

}