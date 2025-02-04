package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Table(name = "customer_pins", indexes = {
        @Index(name = "customer_pins_phone_no_idx", columnList = "phone_no", unique = true),
        @Index(name = "customer_pins_pin_no_idx", columnList = "pin_no")
})
@Entity
@Data
public class CustomerPin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_pin_id", nullable = false)
    private Long id;

    @Column(name = "phone_no", nullable = false, length = 12)
    private String phoneNo;

    @Column(name = "activation_code", length = 12)
    private String activationCode;

    @Column(name = "pin_no", length = 100)
    private String pinNo;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "created_by", nullable = false, length = 20)
    private String createdBy;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "modified_by", length = 20)
    private String modifiedBy;

    @Column(name = "modify_date")
    private LocalDate modifyDate;

}