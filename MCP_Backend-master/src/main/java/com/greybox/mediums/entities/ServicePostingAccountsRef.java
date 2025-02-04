package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.*;

@Table(name = "service_posting_accounts_ref", indexes = {
        @Index(name = "service_posting_accounts_ref_un", columnList = "acct_type", unique = true)
})
@Entity
@Data
public class ServicePostingAccountsRef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "acct_type", nullable = false, length = 50)
    private String itemType;
}