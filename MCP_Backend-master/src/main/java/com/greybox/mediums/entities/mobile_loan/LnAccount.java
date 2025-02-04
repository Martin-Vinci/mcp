package com.greybox.mediums.entities.mobile_loan;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Table(name = "loan_account", indexes = {
        @Index(name = "loan_account_credit_appl_id_idx2", columnList = "credit_appl_id"),
        @Index(name = "loan_account_loan_number_idx", columnList = "loan_number")
})
@Entity
@Data
public class LnAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id", nullable = false)
    private Integer loanId;

    @Column(name = "loan_number", nullable = false, length = 15)
    private String loanNumber;

    @Column(name = "credit_appl_id", nullable = false)
    private Integer creditApplId;

    @Column(name = "approved_amount", nullable = false, precision = 15)
    private BigDecimal approvedAmount;

    @Column(name = "start_date", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate endDate;

    @Column(name = "repay_interest", precision = 15)
    private BigDecimal repayInterest;

    @Column(name = "repay_principal", precision = 15)
    private BigDecimal repayPrincipal;

    @Column(name = "term", nullable = false)
    private Integer term;

    @Column(name = "repay_period", nullable = false, length = 15)
    private String repayPeriod;

    @Column(name = "next_pmt_amount", precision = 15)
    private Double nextPmtAmount;

    @Column(name = "ledger_bal", nullable = false, precision = 15)
    private BigDecimal ledgerBal;

    @Column(name = "next_pmt_date")
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate nextPmtDate;

    @Column(name = "status", nullable = false, length = 15)
    private String status;

    @Column(name = "created_by", nullable = false, length = 10)
    private String createdBy;

    @Column(name = "create_date", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate createDate;

    @Column(name = "modified_by", length = 30)
    private String modifiedBy;

    @Column(name = "modified_date")
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date modifiedDate;

    @Column(name = "row_version", nullable = false)
    private Integer rowVersion;

    @Transient
    private String creditNumber;

    @Transient
    private Integer custId;

    @Transient
    private String custName;

}