package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Table(name = "service_commission", indexes = {
        @Index(name = "service_commission_service_id_idx", columnList = "service_id")
})
@Entity
@Data
public class ServiceCommission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commission_id", nullable = false)
    private Integer commissionId;

    @Column(name = "service_id", nullable = false)
    private Integer serviceId;

    @Column(name = "commission_type", nullable = false, length = 7)
    private String commissionType;

    @Column(name = "amount", precision = 21, scale = 2)
    private BigDecimal amount;

    @Column(name = "calculation_basis", length = 20)
    private String calculationBasis;

    @Column(name = "status", length = 10)
    private String status;

    @Column(name = "create_date")
    private LocalDate createDate;

    @Column(name = "vendor_share", precision = 21, scale = 2)
    private BigDecimal vendorShare;

    @Column(name = "created_by", length = 10)
    private String createdBy;

    @Column(name = "modify_date")
    private LocalDate modifyDate;

    @Column(name = "modified_by", length = 10)
    private String modifiedBy;

}