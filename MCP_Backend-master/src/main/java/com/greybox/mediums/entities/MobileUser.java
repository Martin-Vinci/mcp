package com.greybox.mediums.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.tomcat.jni.Local;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mobile_users", indexes = {
        @Index(name = "mobile_users_phone_number_idx", columnList = "phone_number", unique = true),
        @Index(name = "mobile_users_outlet_code_idx", columnList = "outlet_code"),
        @Index(name = "mobile_users_entity_code_idx", columnList = "entity_code")
})
public class MobileUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "pin", length = 140, updatable = false)
    private String pin;

    @Column(name = "customer_name", length = 140)
    private String customerName;

    @Column(name = "date_created")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateCreated;

    @Column(name = "locked_flag")
    private Boolean lockedFlag;

    @Column(name = "acct_type", length = 20, updatable = false)
    private String acctType;

    @Column(name = "failed_login_count")
    private Integer failedLoginCount;

    @Column(name = "pin_change_flag")
    private Boolean pinChangeFlag;

    @Column(name = "auth_imsi", length = 140)
    private String authImsi;

    @Column(name = "auth_imei", length = 140)
    private String authImei;

    @Column(name = "activation_code")
    private String activationCode;

    @Column(name = "use_android_channel")
    private Boolean useAndroidChannel;

    @Column(name = "use_ussd_channel")
    private Boolean useUssdChannel;

    @Column(name = "wap_otp", length = 100)
    private String wapOtp;

    @Column(name = "wap_otp_expiry")
    private LocalDateTime wapOtpExpiry;

    @Column(name = "approval_status", updatable = false)
    private Boolean approvalStatus;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "approved_by", length = 20)
    private String approvedBy;

    @Column(name = "date_approved")
    private LocalDate dateApproved;

    @Column(name = "birth_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Column(name = "physical_address", length = 200)
    private String physicalAddress;

    @Column(name = "postal_address", length = 50)
    private String postalAddress;

    @Column(name = "gender", length = 8)
    private String gender;

    @Column(name = "entity_code", length = 10, updatable = false)
    private Integer entityCode;

    @Column(name = "outlet_code", length = 15, updatable = false)
    private String outletCode;
    @Column(name = "rsm_id")
    private Integer rsmId;

    @Column(name = "company_id")
    private Integer companyId;

    @Transient
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastTransDate;
    @Transient
    private Integer activeDays;
    @Transient
    private Boolean edit;
    @Transient
    private String reviewAction;
    @Transient
    private List<MobileUserAccount> accountList;
    @Transient
    private LocalDate startDate;
    @Transient
    private LocalDate endDate;
    @Transient
    private String rsmName;
    @Transient
    private BigDecimal witholdingTax;
    @Transient
    private Integer transCount;
    @Transient
    private String account;
    @Transient
    private BigDecimal curBal;
    @Transient
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActivityDate;
}