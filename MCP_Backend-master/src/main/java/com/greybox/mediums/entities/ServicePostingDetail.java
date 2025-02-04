package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

@Table(name = "service_posting_details", indexes = {
        @Index(name = "service_posting_details_service_id_idx", columnList = "service_id")
})
@Entity
@Data
public class ServicePostingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "posting_detail_id", nullable = false)
    private Integer postingDetailId;

    @Column(name = "service_id", nullable = false)
    private Integer serviceId;

    @Column(name = "posting_priority", nullable = false)
    private Integer postingPriority;

    @Column(name = "source_account", nullable = false, length = 100)
    private String sourceAccount;

    @Column(name = "destination_account", nullable = false, length = 100)
    private String destinationAccount;

    @Column(name = "amount_type", nullable = false, length = 50)
    private String amountType;

    @Column(name = "trans_category", nullable = false, length = 50)
    private String transCategory;

    @Column(name = "created_by", nullable = false,updatable = false)
    private String createdBy;

    @Column(name = "create_dt", nullable = false, updatable = false)
    private Date createDt;

    @Column(name = "tran_amt_vendor_share", precision = 18, scale = 5)
    private BigDecimal tranAmtVendorShare;

    @Column(name = "tran_amt_bank_share", precision = 18, scale = 5)
    private BigDecimal tranAmtBankShare;

    @Column(name = "tran_amt_agent_share", precision = 18, scale = 5)
    private BigDecimal tranAmtAgentShare;

    @Column(name = "trust_posting_category")
    private String trustPostingCategory;

    @Transient
    private Boolean edit;

}