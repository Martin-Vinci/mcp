package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Table(name = "cashout_initiations")
@Entity
@Data
public class CashoutInitiation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "date_created", nullable = false)
    private Timestamp dateCreated;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "customer_account", nullable = false)
    private String customerAccount;

    @Column(name = "outlet_code")
    private String outletCode;

    @Column(name = "outlet_phone")
    private String outletPhone;

    @Column(name = "amount", nullable = false, precision = 21, scale = 6)
    private BigDecimal amount;

    @Column(name = "withdraw_code", nullable = false, length = 10)
    private String withdrawCode;

    @Column(name = "approved")
    private Boolean approved;

    @Column(name = "expiry_date", nullable = false)
    private Timestamp expiryDate;

    @Column(name = "customer_name")
    private String customerName;

}