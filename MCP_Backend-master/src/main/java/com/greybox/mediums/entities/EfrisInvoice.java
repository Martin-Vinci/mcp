package com.greybox.mediums.entities;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

@Table(name = "efris_invoice")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EfrisInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id", nullable = false)
    private Integer invoiceId;

    @Column(name = "buyer_tin")
    private String buyerTin;

    @Column(name = "buyer_nin_brn")
    private String buyerNinBrn;

    @Column(name = "buyer_passport_number")
    private String buyerPassportNumber;

    @Column(name = "buyer_legal_name")
    private String buyerLegalName;

    @Column(name = "buyer_business_name")
    private String buyerBusinessName;

    @Column(name = "buyer_address")
    private String buyerAddress;

    @Column(name = "buyer_email")
    private String buyerEmail;

    @Column(name = "buyer_mobile_phone")
    private String buyerMobilePhone;

    @Column(name = "buyer_line_phone")
    private String buyerLinePhone;

    @Column(name = "buyer_place_of_business")
    private String buyerPlaceOfBusiness;

    @Column(name = "buyer_type")
    private String buyerType;

    @Column(name = "buyer_citizenship")
    private String buyerCitizenship;

    @Column(name = "buyer_sector")
    private String buyerSector;

    @Column(name = "buyer_reference_no")
    private String buyerReferenceNo;

    @Column(name = "invoice_no")
    private String invoiceNo;

    @Column(name = "antifake_code")
    private String antifakeCode;

    @Column(name = "device_no")
    private String deviceNo;

    @Column(name = "issued_date")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime issuedDate;

    @Column(name = "efris_operator")
    private String efrisOperator;

    @Column(name = "currency")
    private String currency;

    @Column(name = "origin_invoice_id")
    private String originInvoiceId;

    @Column(name = "invoice_type")
    private String invoiceType;

    @Column(name = "invoice_kind")
    private String invoiceKind;

    @Column(name = "data_source")
    private String dataSource;

    @Column(name = "invoice_industry_code")
    private String invoiceIndustryCode;

    @Column(name = "is_batch")
    private String isBatch;

    @Column(name = "tin")
    private String tin;

    @Column(name = "nin_brn")
    private String ninBrn;

    @Column(name = "legal_name")
    private String legalName;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "address")
    private String address;

    @Column(name = "mobile_phone")
    private String mobilePhone;

    @Column(name = "line_phone")
    private String linePhone;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "place_of_business")
    private String placeOfBusiness;

    @Column(name = "reference_no")
    private String referenceNo;

    @Column(name = "branch_id")
    private String branchId;

    @Column(name = "is_check_reference_no")
    private String isCheckReferenceNo;

    @Column(name = "response_code")
    private String responseCode;

    @Column(name = "invoice_path")
    private String invoicePath;

    @Column(name = "response_message")
    private String responseMessage;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "create_date", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Timestamp createDate;
}