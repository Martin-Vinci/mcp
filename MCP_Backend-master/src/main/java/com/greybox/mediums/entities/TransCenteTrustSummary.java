package com.greybox.mediums.entities;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trans_cente_trust_summary")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransCenteTrustSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "posting_date")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime postingDate;

    @Column(name = "category")
    private String entryType;

    @Column(name = "total_amount")
    private BigDecimal amount;

    @Column(name = "description")
    private String transDescr;

    @Transient
    private String referenceNo;

    @Column(name = "crdb_opp_acct_bal")
    private BigDecimal crdbOppAcctBal;

    @Column(name = "crdb_escrow_acct_bal")
    private BigDecimal crdbEscrowAcctBal;

    @Column(name = "status")
    private String status;

    @Column(name = "message")
    private String message;

    @Transient
    private String drAcctNo;

    @Transient
    private String crAcctNo;

    @Transient
    private Long mainTransId;
}
