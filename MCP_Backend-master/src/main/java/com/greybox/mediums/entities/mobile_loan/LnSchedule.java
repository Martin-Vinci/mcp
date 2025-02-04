package com.greybox.mediums.entities.mobile_loan;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Table(name = "loan_schedule", indexes = {
        @Index(name = "loan_schedule_schedule_no_idx", columnList = "schedule_no"),
        @Index(name = "loan_schedule_loan_id_idx", columnList = "loan_id")
})
@Entity
@Data
public class LnSchedule {
    public LnSchedule(Integer scheduleNo, Integer loanId, Double principalAmount, Double interestAmount,
                      Double principalPaid, Double interestPaid, Double principalUnpaid,
                      Double interestUnpaid, LocalDate dueDate, LocalDate paymentDate, String status,
                      String createdBy, LocalDate createDate, Integer rowVersion) {
        this.scheduleNo = scheduleNo;
        this.loanId = loanId;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.principalPaid = principalPaid;
        this.interestPaid = interestPaid;
        this.principalUnpaid = principalUnpaid;
        this.interestUnpaid = interestUnpaid;
        this.dueDate = dueDate;
        this.paymentDate = paymentDate;
        this.status = status;
        this.createdBy = createdBy;
        this.createDate = createDate;
        this.rowVersion = rowVersion;
    }

    public LnSchedule() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_schedule_id", nullable = false)
    private Long id;

    @Column(name = "schedule_no", nullable = false)
    private Integer scheduleNo;

    @Column(name = "loan_id", nullable = false)
    private Integer loanId;

    @Column(name = "principal_amount", nullable = false, precision = 15)
    private Double principalAmount;

    @Column(name = "interest_amount", nullable = false, precision = 15)
    private Double interestAmount;

    @Column(name = "principal_paid", nullable = false, precision = 15)
    private Double principalPaid;

    @Column(name = "interest_paid", nullable = false, precision = 15)
    private Double interestPaid;

    @Column(name = "principal_unpaid", nullable = false, precision = 15)
    private Double principalUnpaid;

    @Column(name = "interest_unpaid", nullable = false, precision = 15)
    private Double interestUnpaid;

    @Column(name = "due_date", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate dueDate;

    @Column(name = "payment_date")
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate paymentDate;

    @Column(name = "status", nullable = false, length = 15)
    private String status;

    @Column(name = "created_by", nullable = false, length = 10, updatable = false)
    private String createdBy;

    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDate createDate;

    @Column(name = "modified_by", length = 30, insertable = false)
    private String modifiedBy;

    @Column(name = "modified_date", insertable = false)
    private LocalDate modifiedDate;

    @Column(name = "row_version", nullable = false)
    private Integer rowVersion;

    @Transient
    private Double totalAmount;
    @Transient
    private Double amountUnPaid;
    @Transient
    private Double amountPaid;


}