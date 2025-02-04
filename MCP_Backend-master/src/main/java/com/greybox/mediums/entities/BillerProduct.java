package com.greybox.mediums.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "biller_product")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillerProduct {

    @Id
    @Column(name = "biller_prod_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer billerProductId;

    @Column(name = "biller_prod_cat_id", nullable = false)
    private Integer billerProdCatId;

    @Column(name = "biller_id", nullable = false)
    private Integer billerId;

    @Column(name = "description", nullable = false, length = 50)
    private String description;

    @Column(name = "biller_prod_code", nullable = false, length = 50)
    private String billerProdCode;

    @Column(name = "amount", nullable = false, precision = 18, scale = 6)
    private BigDecimal amount;

    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "last_update_date", insertable = false, updatable = false)
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", nullable = false, length = 10)
    private String createdBy;

    @Column(name = "modify_date")
    private LocalDate modifyDate;

    @Column(name = "modified_by", length = 10)
    private String modifiedBy;

    @Transient
    private String categoryDescr;
    @Transient
    private String description2;
    @Transient
    private String billerCode;
    @Transient
    private String channelSource;

}