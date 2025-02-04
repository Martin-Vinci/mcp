package com.greybox.mediums.entities.mobile_loan;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Table(name = "loan_repayment_info")
@Entity
@Data
public class LnRepaymentInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settings_id", nullable = false)
    private Integer settingsId;

    @Column(name = "int_calc_option", nullable = false, length = 15)
    private String intCalcOption;

    @Column(name = "repayment_type", nullable = false)
    private String repaymentType;

    @Column(name = "interest_rate", nullable = false, precision = 15)
    private Double interestRate;

    @Column(name = "created_by", nullable = false, length = 10)
    private String createdBy;

    @Column(name = "create_date", nullable = false, updatable = false)
    private Date createDate;

    @Column(name = "modified_by", length = 30)
    private String modifiedBy;

    @Column(name = "modified_date", insertable = false)
    private Date modifiedDate;

    @Column(name = "row_version", nullable = false)
    private Integer rowVersion;

}