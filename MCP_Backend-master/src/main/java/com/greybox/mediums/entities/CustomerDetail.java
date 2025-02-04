package com.greybox.mediums.entities;

import com.greybox.mediums.models.OutletAuthRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "customer_details", indexes = {
        @Index(name = "customer_details_mobile_phone_idx", columnList = "mobile_phone", unique = true)
})
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class CustomerDetail extends OutletAuthRequest {
    @Id
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String surName;

    @Column(name = "birth_dt", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "town_id", nullable = false, length = 100)
    private String town;

    @Column(name = "ident_id", length = 5)
    private String idType;

    @Column(name = "id_value", length = 40)
    private String idNumber;

    @Column(name = "title_id", length = 5)
    private String titleId;

    @Column(name = "mobile_phone", nullable = false, length = 20)
    private String mobilePhone;

    @Column(name = "id_expiry_dt")
    private LocalDate idExpiryDate;

    @Column(name = "id_issue_dt")
    private LocalDate idIssueDt;

    @Column(name = "gender", nullable = false, length = 10)
    private String gender;

    @Column(name = "outlet_code", nullable = false, length = 15)
    private String outletNo;

    @Column(name = "nok_name", length = 100)
    private String nokName;

    @Column(name = "nok_phone", length = 20)
    private String nokPhone;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "customer_photo")
    private byte[] customerPhoto;

    @Column(name = "customer_signature")
    private byte[] customerSignature;

    @Transient
    private String photoBase64String;
    @Transient
    private String signatureBase64String;


}