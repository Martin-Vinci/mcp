package com.greybox.mediums.entities.mobile_loan;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Table(name = "mbl_customer")
@Entity
@Data
public class LnCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cust_id", nullable = false)
    private Integer id;

    @Column(name = "first_name", nullable = false, length = 30)
    private String firstName;

    @Column(name = "middle_name", length = 30)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 30)
    private String lastName;

    @Column(name = "title", length = 10)
    private String title;

    @Column(name = "status", nullable = false, length = 15)
    private String status;

    @Column(name = "occupation", nullable = false, length = 50)
    private String occupation;

    @Column(name = "email_address", length = 50)
    private String emailAddress;

    @Column(name = "latitudes", length = 15)
    private String latitudes;

    @Column(name = "longitudes", length = 15)
    private String longitudes;

    @Column(name = "row_version", nullable = false)
    private Integer rowVersion;

    @Column(name = "birth_dt", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate birthDt;

    @Column(name = "place_of_residence", nullable = false, length = 100)
    private String placeOfResidence;

    @Column(name = "fcs_no", length = 20)
    private String fcsNo;

    @Column(name = "marital_status", nullable = false, length = 10)
    private String maritalStatus;

    @Column(name = "business_type", nullable = false, length = 100)
    private String businessType;

    @Column(name = "id_type", length = 30)
    private String idType;

    @Column(name = "id_no", length = 16)
    private String idNo;

    @Column(name = "next_of_kin_name", nullable = false, length = 30)
    private String nextOfKinName;

    @Column(name = "next_of_kin_address", nullable = false, length = 50)
    private String nextOfKinAddress;

    @Column(name = "gps", length = 30)
    private String gps;

    @Column(name = "phone_no", nullable = false, length = 16)
    private String phoneNo;

    @Column(name = "create_by", nullable = false, length = 10)
    private String createBy;

    @Column(name = "create_date", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate createDate;

    @Column(name = "modified_date")
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate modifiedDate;

    @Column(name = "modified_by", length = 10)
    private String modifiedBy;

    @Column(name = "password", length = 50)
    private String password;

    @Transient
    private Boolean edit;
}