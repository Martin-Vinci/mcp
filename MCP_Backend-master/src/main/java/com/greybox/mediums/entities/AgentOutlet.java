package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Table(name = "agent_outlets", indexes = {
        @Index(name = "agent_outlets_agent_id_idx", columnList = "agent_id"),
        @Index(name = "agent_outlets_outlet_no_idx", columnList = "outlet_no", unique = true),
        @Index(name = "agent_outlets_phone_no_idx", columnList = "phone_no", unique = true)
})
@Entity
@Data
public class AgentOutlet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outlet_id", nullable = false)
    private Integer outletId;

    @Column(name = "outlet_name", nullable = false, length = 200)
    private String outletName;

    @Column(name = "outlet_no", nullable = false, length = 15)
    private String outletNo;

    @Column(name = "agent_id", nullable = false)
    private Integer agentId;

    @Column(name = "status", nullable = false, length = 8)
    private String status;

    @Column(name = "phone_no", nullable = false, length = 20)
    private String phoneNo;

    @Column(name = "point_of_operation", nullable = false)
    private String pointOfOperation;

    @Column(name = "contact_person", length = 40)
    private String contactPerson;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "acct_no", nullable = false)
    private String floatAcctNo;

    @Column(name = "longitude", length = 30)
    private String longitude;

    @Column(name = "latitude", length = 30)
    private String latitude;

    @Column(name = "device_imei", length = 200)
    private String deviceImei;

    @Column(name = "device_imsi", length = 200)
    private String deviceImsi;

    @Column(name = "device_id", length = 200)
    private String deviceId;

    @Column(name = "supervised_by", length = 20)
    private String supervisedBy;

    @Column(name = "supervised_dt")
    private LocalDate supervisedDt;

    @Column(name = "created_by", nullable = false, length = 20)
    private String createdBy;

    @Column(name = "create_dt", nullable = false)
    private Date createDt;

    @Column(name = "modify_by", length = 20)
    private String modifyBy;

    @Column(name = "modify_dt")
    private Date modifyDt;

    @Transient
    private Boolean edit;

}