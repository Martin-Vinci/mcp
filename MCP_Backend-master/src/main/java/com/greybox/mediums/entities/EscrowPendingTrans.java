package com.greybox.mediums.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Table(name = "escrow_pending_trans", indexes = {
        @Index(name = "escrow_pending_trans_cr_acct_no_idx", columnList = "cr_acct_no"),
        @Index(name = "escrow_pending_trans_util_posted_idx", columnList = "util_posted"),
        @Index(name = "escrow_pending_trans_dr_acct_no_idx", columnList = "dr_acct_no")
})
@Entity
@Data
public class EscrowPendingTrans {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trans_id", nullable = false)
    private Long transId;

    @Column(name = "cr_acct_no", length = 30, updatable = false)
    private String crAcctNo;

    @Column(name = "dr_acct_no", length = 30, updatable = false)
    private String drAcctNo;

    @Column(name = "amount", precision = 21, scale = 6, updatable = false)
    private BigDecimal amount;

    @Column(name = "iso_code", length = 5, updatable = false)
    private String isoCode;

    @Column(name = "posted_by", length = 20, updatable = false)
    private String postedBy;

    @Column(name = "trans_descr", length = 150, updatable = false)
    private String transDescr;

    @Column(name = "success_flag", length = 1)
    private String successFlag;

    @Column(name = "trans_date", updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Timestamp transDate;

    @Column(name = "util_posted", length = 2)
    private String utilPosted;

    @Column(name = "reversal_flag", length = 2)
    private String reversalFlag;

    @Column(name = "reversal_reason", length = 200)
    private String reversalReason;

    @Column(name = "external_trans_ref", length = 20, updatable = false)
    private String externalTransRef;

    @Transient
    private String action;
}