
package com.greybox.mediums.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.greybox.mediums.models.OutletAuthRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "payment_request")
 public class PaymentRequest extends OutletAuthRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_rqst_id", nullable = false)
    private Integer requestId;
    @Column(name = "from_phone")
    private String fromPhone;
    @Column(name = "requester_phone")
    private String requesterPhone;
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "customer_name")
    private String customerName;
    @Column(name = "requester_reason")
    private String requesterReason;
    @Column(name = "status")
    private String status;
    @Column(name = "create_dt", nullable = false)
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createDate;
    @Column(name = "modify_by")
    private String modifyBy;
    @Column(name = "modify_dt")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDate modifyDate;
    @Transient
    private String action;
}