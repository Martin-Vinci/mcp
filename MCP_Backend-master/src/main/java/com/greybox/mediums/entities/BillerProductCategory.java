package com.greybox.mediums.entities;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;

@Table(name = "biller_prod_category")
@Entity
@Data
public class BillerProductCategory {

    @Id
    @Column(name = "biller_prod_cat_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer billerProductCategoryId;

    @Column(name = "description", nullable = false, length = 50)
    private String description;

    @Column(name = "biller_id", nullable = false)
    private Integer billerId;

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