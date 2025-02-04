package com.greybox.mediums.entities;

import lombok.Data;
import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Table(name = "issued_receipts", indexes = {
        @Index(name = "issued_receipts_outlet_code_idx", columnList = "outlet_code"),
        @Index(name = "issued_receipts_txn_id_idx", columnList = "txn_id")
})
@Entity
@Data
public class IssuedReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id", nullable = false)
    private Integer receiptId;

    @Column(name = "receipt_number", insertable = false)
    private String receiptNumber;

    @Column(name = "date_created", insertable = false)
    private Instant dateCreated;

    @Column(name = "issuer")
    private String issuer;

    @Column(name = "outlet_code")
    private String outletCode;

    @Column(name = "receipt_data")
    private String receiptData;

    @Column(name = "txn_id")
    private Integer txnId;

    @Column(name = "part_date", insertable = false)
    private LocalDate partDate;

    @Transient
    private LocalDate fromDate;
    @Transient
    private LocalDate toDate;


}