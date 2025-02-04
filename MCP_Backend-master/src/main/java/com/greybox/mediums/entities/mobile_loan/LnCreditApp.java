package com.greybox.mediums.entities.mobile_loan;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Table(name = "mbl_credit_app")
@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LnCreditApp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credit_app_id", nullable = false)
    private Integer id;

    @Column(name = "credit_type", nullable = false, length = 20)
    private String creditType;

    @Column(name = "start_date", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate endDate;

    @Column(name = "repay_term", nullable = false)
    private Integer repayTerm;

    @Column(name = "repay_period", nullable = false, length = 15)
    private String repayPeriod;

    @Column(name = "nxt_pmt_amt")
    private BigDecimal nextPmtAmt;

    @Column(name = "status")
    private String status;

    @Column(name = "cust_id", nullable = false)
    private Integer custId;

    @Column(name = "created_by", nullable = false, length = 10, updatable = false)
    private String createdBy;

    @Column(name = "create_date", nullable = false, updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate createDate;

    @Column(name = "modified_by", length = 30, insertable = false)
    private String modifiedBy;

    @Column(name = "modified_date", insertable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate modifiedDate;

    @Column(name = "row_version", updatable = false)
    private Integer rowVersion;

    @Column(name = "appl_amt", nullable = false, precision = 6)
    private BigDecimal applAmt;

    @Column(name = "loan_purpose")
    private String loanPurpose;

    @Transient
    private String customerName;


}