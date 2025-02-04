package com.greybox.mediums.entities.mobile_loan;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Table(name = "loan_repayment_history", indexes = {
        @Index(name = "loan_repayment_history_un", columnList = "txn_ref", unique = true)
})
@Entity
@Data
public class LoanRepaymentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trans_id", nullable = false)
    private Integer transId;

    @Column(name = "acct_no", nullable = false, length = 30)
    private String acctNo;

    @Column(name = "txn_ref", nullable = false, length = 30)
    private String txnRef;

    @Column(name = "trans_amount", nullable = false, precision = 21, scale = 6)
    private BigDecimal transAmount;

    @Column(name = "charge_amount", nullable = false, precision = 21, scale = 6)
    private BigDecimal chargeAmount;

    @Column(name = "channel_code", nullable = false)
    private String channelCode;

    @Column(name = "posted_by", length = 20)
    private String postedBy;

    @Column(name = "initiator_phone_no", length = 20)
    private String initiatorPhoneNo;

    @Column(name = "trans_descr", length = 150)
    private String transDescr;

    @Column(name = "success_flag", length = 1)
    private String successFlag;

    @Column(name = "trans_date", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Timestamp transDate;

    @Column(name = "reversal_flag", length = 2)
    private String reversalFlag;

    @Column(name = "reversal_reason", length = 200)
    private String reversalReason;

    @Column(name = "external_trans_ref", length = 20)
    private String externalTransRef;

}