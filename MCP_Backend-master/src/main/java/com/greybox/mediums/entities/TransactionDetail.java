package com.greybox.mediums.entities;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "transaction_details", indexes = {
        @Index(name = "transaction_details_main_trans_id_idx", columnList = "main_trans_id"),
        @Index(name = "transaction_details_trans_type_idx", columnList = "trans_type"),
        @Index(name = "transaction_details_dr_acct_no_idx", columnList = "dr_acct_no"),
        @Index(name = "transaction_details_cr_acct_no_idx", columnList = "cr_acct_no")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trans_id", nullable = false)
    private Long id;

    @Column(name = "main_trans_id", nullable = false, precision = 12)
    private Long mainTransId;

    @Column(name = "cr_acct_no", nullable = false, length = 30)
    private String crAcctNo;

    @Column(name = "amount", nullable = false, precision = 21, scale = 6)
    private BigDecimal amount;

    @Column(name = "iso_code", nullable = false, length = 3)
    private String isoCode;

    @Column(name = "dr_acct_no", length = 30)
    private String drAcctNo;

    @Column(name = "trans_type", nullable = false, length = 30)
    private String transType;

    @Column(name = "trans_descr", length = 80)
    private String transDescr;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "posting_dt", nullable = false)
    private Timestamp postingDt;

    @Column(name = "reversal_fg", length = 1)
    private String reversalFg;

    @Column(name = "reversal_message", length = 80)
    private String reversalMessage;

    @Column(name = "item_no", nullable = false)
    private Integer itemNo;

    @Column(name = "create_date", nullable = false)
    private Timestamp createDate;

    @Column(name = "created_by", nullable = false, length = 10)
    private String createdBy;

    @Column(name = "modify_date")
    private Date modifyDate;

    @Column(name = "modified_by", length = 10)
    private String modifiedBy;

    @Transient
    private LocalDate createDt;

    @Transient
    private String entryType;

    @Transient
    private String initiatorPhoneNo;
}
