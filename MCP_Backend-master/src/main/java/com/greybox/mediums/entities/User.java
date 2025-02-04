package com.greybox.mediums.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_ref", indexes = {
        @Index(name = "user_ref_user_role_id_idx", columnList = "user_role"),
        @Index(name = "user_ref_user_name_idx", columnList = "user_name", unique = true)
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Integer employeeId;

    @Column(name = "user_name", length = 15)
    private String userName;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "user_role", nullable = false)
    private Integer userRoleId;

    @Column(name = "email_address", length = 100)
    private String emailAddress;

    @Column(name = "phone_no", length = 20)
    private String phoneNo;

    @Column(name = "receive_biller_stmnt", nullable = false, length = 1)
    private String receiveBillerStmnt;

    @Column(name = "lock_user", nullable = false, length = 1)
    private String lockUser;

    @Column(name = "pwd_enhanced_flag", length = 1)
    private String pwdEnhancedFlag;

    @Column(name = "user_pwd", nullable = false, length = 20, updatable = false)
    private String userPwd;

    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @Column(name = "created_by", nullable = false, length = 20, updatable = false)
    private String createdBy;

    @Column(name = "create_dt", nullable = false, updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate createDt;

    @Column(name = "modify_by", length = 20)
    private String modifyBy;

    @Column(name = "modify_dt")
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate modifyDt;

    @Transient
    private Boolean edit;


}