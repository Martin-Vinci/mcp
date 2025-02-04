package com.greybox.mediums.entities;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "biller_ref")
@Entity
@Data
public class Biller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "biller_ref_id", nullable = false)
    private Integer id;

    @Column(name = "biller_code", length = 20)
    private String billerCode;

    @Column(name = "description", length = 150)
    private String description;

    @Column(name = "acct_no", length = 20)
    private String acctNo;

    @Column(name = "end_point_url", length = 200)
    private String endpointUrl;

    @Column(name = "vendor_code", length = 100)
    private String vendorCode;

    @Column(name = "vendor_password", length = 100)
    private String vendorPassword;

    @Column(name = "sms_template", length = 500)
    private String smsTemplate;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "last_update_date", insertable = false, updatable = false)
    private LocalDateTime lastUpdateDate;

}