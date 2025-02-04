package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Table(name = "outlet_withdraw_initiations")
@Entity
@Data
public class OutletWithdrawInitiation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "date_created", nullable = false)
    private Timestamp dateCreated;

    @Column(name = "super_agent_code")
    private String superAgentCode;

    @Column(name = "super_agent_phone")
    private String superAgentPhone;

    @Column(name = "outlet_account")
    private String outletAccount;

    @Column(name = "outlet_code", nullable = false)
    private String outletCode;

    @Column(name = "outlet_phone", nullable = false)
    private String outletPhone;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "approved")
    private Boolean approved;

    @Column(name = "expiry_date")
    private Timestamp expiryDate;

    @Column(name = "withdraw_code", nullable = false, length = 10)
    private String withdrawCode;

    @Column(name = "outlet_name")
    private String outletName;
}