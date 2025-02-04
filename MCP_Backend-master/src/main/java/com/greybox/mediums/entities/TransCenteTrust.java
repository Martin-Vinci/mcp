package com.greybox.mediums.entities;

import com.greybox.mediums.utils.DataUtils;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "trans_cente_trust")
@Entity
@Data
public class TransCenteTrust {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trans_id", nullable = false)
    private Long transId;

    @Column(name = "main_trans_id")
    private Long mainTransId;

    @Column(name = "dr_acct_no")
    private String drAcctNo;

    @Column(name = "description")
    private String description;

    @Column(name = " trans_type")
    private String transType;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "cr_acct_no")
    private String crAcctNo;

    @Column(name = "create_date")
    private LocalDate createDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = " reference_no")
    private String referenceNo;

    @Column(name = "status")
    private String status;

    @Column(name = "crdb_posting_dt")
    private LocalDateTime crdbPostingDt;

    public TransCenteTrust() {}

    public TransCenteTrust(Long mainTransId, String drAcctNo, String crAcctNo, BigDecimal amount, String description, String createdBy, String status, String transType, String referenceNo) {
        this.mainTransId = mainTransId;
        this.drAcctNo = drAcctNo;
        this.description = description;
        this.amount = amount;
        this.crAcctNo = crAcctNo;
        this.transType = transType;
        this.createDate = DataUtils.getCurrentDate().toLocalDate();
        this.createdBy = createdBy;
        this.status = status;
        this.referenceNo = referenceNo;
    }
}

