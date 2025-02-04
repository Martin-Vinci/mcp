package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.*;

@Table(name = "service_posting_amount_ref", indexes = {
        @Index(name = "service_posting_amount_ref_un", columnList = "amount_type", unique = true)
})
@Entity
@Data
public class ServicePostingAmountRef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "amount_type", nullable = false, length = 50)
    private String itemType;
}