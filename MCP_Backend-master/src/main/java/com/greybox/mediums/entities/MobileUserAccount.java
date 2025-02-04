package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "mobile_user_accounts")
public class MobileUserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "date_added")
    private Timestamp dateAdded;

    @Column(name = "user_id")
    private long mobileUserId;

    @Column(name = "account")
    private String acctNo;

    @Column(name = "account_title")
    private String description;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "balance_update_dt")
    private LocalDate balanceUpdateDate;

    @Column(name = "account_type")
    private String acctType;

    @Column(name = "cur_bal")
    private BigDecimal currentBalance;

    @Transient
    private String balanceLevels;
}
