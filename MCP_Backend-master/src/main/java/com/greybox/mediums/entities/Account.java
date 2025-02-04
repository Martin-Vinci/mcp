package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "accounts", indexes = {
        @Index(name = "accounts_entity_id_idx", columnList = "entity_id"),
        @Index(name = "accounts_agent_id_idx", columnList = "agent_id"),
        @Index(name = "accounts_entity_code_idx", columnList = "entity_code"),
        @Index(name = "accounts_acct_no_idx", columnList = "acct_no", unique = true)
})
@Entity
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "acct_id", nullable = false)
    private Integer acctId;

    @Column(name = "acct_no", nullable = false, length = 20)
    private String acctNo;

    @Column(name = "entity_code", nullable = false, length = 20)
    private String entityCode;

    @Column(name = "entity_id", nullable = false)
    private Integer entityId;

    @Column(name = "agent_id")
    private Integer agentId;

    @Column(name = "ledger_bal", nullable = false)
    private BigDecimal ledgerBal;

    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @Column(name = "created_by", nullable = false, length = 20, updatable = false)
    private String createdBy;

    @Column(name = "create_dt", nullable = false, updatable = false)
    private Date createDt;

    @Column(name = "modify_by", length = 20, insertable = false)
    private String modifyBy;

    @Column(name = "modify_dt", insertable = false)
    private Date modifyDt;

}