package com.greybox.mediums.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Table(name = "customer_ref")
@Entity
@Data
public class CustomerRef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "gender", nullable = false, length = 8)
    private String gender;

    @Column(name = "phone_no", nullable = false, length = 20)
    private String phoneNo;

    @Column(name = "id_no", length = 20)
    private String idNo;

    @Transient
    private String acctNo;

    @Column(name = "birth_dt", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date birthDt;

    @Column(name = "address_line", nullable = false, length = 100)
    private String addressLine;

    @Column(name = "pin_no", updatable = false)
    private String pinNo;

    @Column(name = "device_imei", length = 100)
    private String deviceImei;

    @Column(name = "device_imsi", length = 100)
    private String deviceImsi;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "failed_login_times")
    private Integer failedLoginTimes;

    @Column(name = "pin_enhanced_flag", length = 1)
    private String pinEnhancedFlag;

    @Column(name = "pin_locked", length = 1)
    private String pinLocked;

    @Column(name = "enable_app", length = 1)
    private String enableApp;

    @Column(name = "enable_ussd", length = 1)
    private String enableUssd;

    @Column(name = "create_dt", nullable = false, updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date createDt;

    @Transient
    private Boolean edit;
}