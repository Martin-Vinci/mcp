package com.greybox.mediums.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Entity
@Table(name = "trans_batch")
@Data
public class TransBatch implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "dr_acct_no", nullable = false)
    private String drAcctNo;

    @Column(name = "service_code", nullable = false)
    private Integer serviceCode;

    @Column(name = "payment_code")
    private String paymentCode;

    @Column(name = "biller_code")
    private String billerCode;

    @Column(name = "initiator_phone")
    private String initiatorPhone;

    @Column(name = "item_uuid", updatable = false, unique = true)
    private String itemUuid;

    @Column(name = "create_date", updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    @Column(name = "status", nullable = false)
    private String status;
 
    @Transient
    private String description;

    @Transient
    private List<TransBatchItem> transBatchItems;
}
