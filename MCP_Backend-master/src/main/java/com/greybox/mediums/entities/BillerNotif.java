package com.greybox.mediums.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "biller_notif")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillerNotif {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "biller_notif_id", nullable = false)
    private Integer id;

    @Column(name = "amount", updatable = false)
    private BigDecimal amount;

    @Column(name = "iso_code", updatable = false)
    private String isoCode;

    @Column(name = "posted_by", updatable = false)
    private String postedBy;

    @Column(name = "biller_code", updatable = false)
    private String billerCode;

    @Column(name = "trans_descr", updatable = false)
    private String transDescr;

    @Column(name = "status")
    private String status;

    @Column(name = "mcp_trans_id", updatable = false)
    private Long transId;

    @Column(name = "trans_date", updatable = false)
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime transDate;

    @Column(name = "reversal_flag")
    private String reversalFlag;
    @Column(name = "reversal_reason")
    private String reversalReason;
    @Column(name = "reference_no", updatable = false)
    private String referenceNo;
    @Column(name = "phone_from", updatable = false)
    private String initiatorPhone;
    @Column(name = "vendor_trans_id")
    private String extenalTransRef;
    @Column(name = "third_party_reference", updatable = false)
    private String thirdPartyReference;
    @Column(name = "response_data")
    private String responseData;
    @Column(name = "request_data", updatable = false)
    private String requestData;
    @Column(name = "processing_duration")
    private Integer processingDuration;
    @Column(name = "channel_code", updatable = false)
    private String channelCode;
    @Transient
    private java.sql.Date startDate;
    @Transient
    private java.sql.Date endDate;



}