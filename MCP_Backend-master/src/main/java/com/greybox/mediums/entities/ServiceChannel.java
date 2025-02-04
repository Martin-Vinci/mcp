package com.greybox.mediums.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.greybox.mediums.models.TxnResult;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Table(name = "service_channel")
@Entity
@Data
public class ServiceChannel {

    @Column(name = "channel_id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer channelId;

    @Column(name = "channel_code", nullable = false)
    private String channelCode;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "channel_username", nullable = false)
    private String channelUsername;

    @Column(name = "channel_password", nullable = false)
    private String channelPassword;

    @Column(name = "enforce_pwd_expiry", nullable = false)
    private String enforcePwdExpiry;

    @Column(name = "expiry_date")
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate expiryDate;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "create_dt", nullable = false, updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate createDt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "modified_dt", insertable = false)
    private LocalDate modifiedDate;

    @Column(name = "modified_by", insertable = false)
    private String modifiedBy;

    public TxnResult validateFields() {
        if (channelCode == null || channelCode.isEmpty()) {
            return TxnResult.builder().message("Channel code is required").code("-99").build();
        }
        if (description == null || description.isEmpty()) {
            return TxnResult.builder().message("Description is required").code("-99").build();
        }
        if (channelUsername == null || channelUsername.isEmpty()) {
            return TxnResult.builder().message("Channel username is required").code("-99").build();
        }
        if (channelPassword == null || channelPassword.isEmpty()) {
            return TxnResult.builder().message("Channel password is required").code("-99").build();
        }
        if (enforcePwdExpiry == null || enforcePwdExpiry.isEmpty()) {
            return TxnResult.builder().message("Enforce password expiry is required").code("-99").build();
        }
        if (status == null || status.isEmpty()) {
            return TxnResult.builder().message("Status is required").code("-99").build();
        }
        if (createDt == null) {
            return TxnResult.builder().message("Create date is required").code("-99").build();
        }
        if (createdBy == null || createdBy.isEmpty()) {
            return TxnResult.builder().message("Created by is required").code("-99").build();
        }
        // If all validations pass, return null or success
        return TxnResult.builder().message("Validation passed").code("00").build();
    }


}