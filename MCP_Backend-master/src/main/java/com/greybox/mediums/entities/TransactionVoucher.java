package com.greybox.mediums.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.greybox.mediums.models.TransRespData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transaction_voucher")
public class TransactionVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "voucher_code", nullable = false, length = 12)
    private String voucherCode;

    @Column(name = "source_phone_no", nullable = false, length = 15)
    private String sourcePhoneNo;

    @Column(name = "recipient_phone_no", nullable = false, length = 15)
    private String recipientPhoneNo;

    @Column(name = "narration", nullable = false, length = 200)
    private String narration;

    @Column(name = "buy_trans_id")
    private Long buyTransId;

    @Column(name = "redeem_trans_id")
    private Long redeemTransId;

    @Column(name = "expiry_date", nullable = false)
    @JsonFormat(pattern="dd-MM-yyyy HH:mm:ss")
    private LocalDateTime expiryDate;

    @JsonFormat(pattern="dd-MM-yyyy")
    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "status", nullable = false, length = 12)
    private String status;

    @Transient
    private TransRespData transRespData;
    @Transient
    private Date startDate;
    @Transient
    private Date endDate;

}