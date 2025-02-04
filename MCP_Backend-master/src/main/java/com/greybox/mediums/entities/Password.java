package com.greybox.mediums.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Table(name = "system_user_pwd")
@Data
@Entity
@NoArgsConstructor
public class Password {
    @Id
    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Column(name = "encrypted_pwd")
    private String encryptedPswd;

    @Column(name = "status")
    private String status;

    @Column(name = "ptid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ptid;

    @Column(name = "password_cycle")
    private Integer passwordCycle;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "create_date")
    private Timestamp createDate;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modify_date")
    private Timestamp modifyDate;

}