package com.greybox.mediums.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

@Table(name = "service_ref", indexes = {
        @Index(name = "service_ref_biller_code_idx", columnList = "biller_code"),
        @Index(name = "service_ref_service_code_idx", columnList = "service_code", unique = true)
})
@Entity
@Data
public class ServiceRef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id", nullable = false)
    private Integer serviceId;

    @Column(name = "service_code", updatable = false)
    private Integer serviceCode;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "trans_type", length = 15)
    private String transType;

    @Column(name = "biller_acct_no", length = 15)
    private String billerAcctNo;

    @Column(name = "charge_acct_no", length = 15)
    private String bankIncomeAcctNo;

    @Column(name = "transit_acct_no", length = 15)
    private String transitAcctNo;

    @Column(name = "expense_acct_no", length = 15)
    private String expenseAcctNo;

    @Column(name = "debit_credit", length = 10)
    private String debitCredit;

    @Column(name = "status", length = 10)
    private String status;

    @Column(name = "min_trans_amt",nullable = false)
    private BigDecimal minTransAmt;

    @Column(name = "max_trans_amt",nullable = false)
    private BigDecimal maxTransAmt;

    @Column(name = "daily_withdraw_limit")
    private BigDecimal dailyWithdrawLimit;

    @Column(name = "mobile_money_tax")
    private BigDecimal mobileMoneyTaxPercentage;

    @Column(name = "trans_literal", length = 20)
    private String transLiteral;

    @Column(name = "biller_code", length = 30)
    private String billerCode;
    @Column(name = "sys_maintain_percent")
    private BigDecimal maintainCommissionPercentage;
    @Column(name = "maintain_calc_basis")
    private String maintenanceCalculationBasis;
    @Column(name = "maintenance_acct")
    private String maintenanceAccount;
    @Column(name = "create_date", updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate createDate;

    @Column(name = "created_by", length = 10, updatable = false)
    private String createdBy;

    @Column(name = "modify_date", insertable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date modifyDate;

    @Column(name = "modified_by", length = 10, insertable = false)
    private String modifiedBy;

    @Column(name = "service_category", insertable = false, updatable = false)
    private String serviceCategory;

    @Transient
    private Boolean edit;

}