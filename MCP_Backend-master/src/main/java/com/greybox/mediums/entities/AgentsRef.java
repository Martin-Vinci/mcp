package com.greybox.mediums.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.sql.Date;
import java.time.LocalDate;

@Table(name = "agents_ref", indexes = {
        @Index(name = "agents_ref_phone_no_idx", columnList = "phone_no"),
        @Index(name = "agents_ref_agent_code_idx", columnList = "agent_code", unique = true)
})
@Entity
@Data
public class AgentsRef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agent_id", nullable = false)
    private Integer agentId;

    @Column(name = "agent_code", nullable = false)
    private Integer agentCode;

    @Column(name = "agent_name", nullable = false, length = 100)
    private String agentName;

    @Column(name = "phone_no", length = 15)
    private String phoneNo;

    @Column(name = "tin_no", length = 20)
    private String tinNo;

    @Column(name = "registration_dt", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate registrationDt;

    @Column(name = "registration_no", length = 20)
    private String registrationNo;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "comm_acct_no", length = 20)
    private String commAcctNo;

    @Column(name = "address_line_1", nullable = false, length = 100)
    private String addressLine1;

    @Column(name = "email_address", length = 100)
    private String emailAddress;

    @Column(name = "supervised_by", length = 20)
    private String supervisedBy;

    @Column(name = "supervised_dt")
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate supervisedDt;

    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @Column(name = "created_by", nullable = false, length = 20)
    private String createdBy;

    @Column(name = "create_dt", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date createDt;

    @Column(name = "modify_by", length = 20)
    private String modifyBy;

    @Column(name = "modify_dt")
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date modifyDt;

    @Transient
    private Boolean edit;

}