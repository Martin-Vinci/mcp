package com.greybox.mediums.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Entity
@Table(name = "trans_batch_item")
@Data
public class TransBatchItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_item_id", nullable = false)
    private Long batchItemId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "customer_phone_no")
    private String customerPhoneNo;

    @Column(name = "customer_account")
    private String customerAccount;

    @Column(name = "trans_amt", nullable = false)
    private BigDecimal transAmt;

    @Column(name = "description")
    private String description;

    @Column(name = "reference_no")
    private String referenceNo;

    @Column(name = "customer_area")
    private String customerArea;

    @Column(name = "customer_type")
    private String customerType;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "create_date", updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    @Column(name = "status")
    private String status;

    @Column(name = "message")
    private String message;
}
