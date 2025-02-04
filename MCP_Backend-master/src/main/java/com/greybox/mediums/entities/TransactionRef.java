package com.greybox.mediums.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;

@Table(name = "transaction_ref", indexes = {
        @Index(name = "transactions_ref_dr_acct_no_idx", columnList = "dr_acct_no"),
        @Index(name = "transactions_ref_util_posted_idx", columnList = "util_posted"),
        @Index(name = "transactions_ref_service_code_idx", columnList = "service_code"),
        @Index(name = "transactions_ref_cr_acct_no_idx", columnList = "cr_acct_no")
})
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trans_id", nullable = false)
    private Long id;

    @Column(name = "cr_acct_no", length = 30)
    private String crAcctNo;

    @Column(name = "dr_acct_no", length = 30)
    private String drAcctNo;

    @Column(name = "amount", precision = 21, scale = 6)
    private BigDecimal amount;

    @Column(name = "iso_code", length = 5)
    private String isoCode;

    @Column(name = "posted_by", length = 20)
    private String postedBy;

    @Column(name = "service_code")
    private Integer serviceCode;

    @Column(name = "trans_descr", length = 150)
    private String transDescr;

    @Column(name = "success_flag", length = 1)
    private String successFlag;

    @Column(name = "trans_date")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime transDate;

    @Column(name = "util_posted", length = 2)
    private String utilPosted;

    @Column(name = "reversal_flag", length = 2)
    private String reversalFlag;

    @Column(name = "reversal_reason", length = 200)
    private String reversalReason;

    @Column(name = "external_trans_ref", length = 20)
    private String externalTransRef;

    @Column(name = "depositor_phone", length = 30)
    private String depositorPhone;

    @Column(name = "depositor_name", length = 80)
    private String depositorName;

    @Column(name = "agent_commission", precision = 21, scale = 6)
    private BigDecimal agentCommission;

    @Column(name = "total_charge", precision = 21, scale = 6)
    private BigDecimal totalCharge;

    @Column(name = "excise_duty", precision = 21, scale = 6)
    private BigDecimal exciseDuty;

    @Column(name = "withhold_tax", precision = 21, scale = 6)
    private BigDecimal withholdTax;

    @Column(name = "system_date")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime systemDate;

    @Column(name = "external_bank_code", length = 20)
    private String externalBankCode;

    @Column(name = "external_acct", length = 30)
    private String externalAcct;

    @Column(name = "initiator_phone_no", length = 20)
    private String initiatorPhoneNo;

    @Column(name = "misc_value_1", length = 50) private String transactionMode;

    @Column(name = "misc_value_2", length = 50)
    private String miscValue2;

    @Column(name = "misc_value_3", length = 50)
    private String miscValue3;

    @Column(name = "misc_value_4", length = 50)
    private String miscValue4;

    @Column(name = "misc_value_5", length = 50)
    private String miscValue5;

    @Column(name = "misc_value_6", length = 50)
    private String miscValue6;

    @Column(name = "misc_value_7", length = 50)
    private String miscValue7;

    @Column(name = "misc_value_8", length = 50)
    private String miscValue8;

    @Column(name = "misc_value_9", length = 50)
    private String miscValue9;

    @Column(name = "misc_value_10", length = 50)
    private String miscValue10;

    @Transient
    private BigDecimal bankCommission;
    @Transient
    private String transType;
    @Transient
    private Date startDate;
    @Transient
    private Date endDate;
}