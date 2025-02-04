package com.greybox.mediums.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.Type;
import javax.persistence.*;
import java.util.Date;

@Table(name = "biller_notif_log")
@Entity
@Data
public class BillerNotifLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notif_log_id", nullable = false)
    private Integer id;

    @Column(name = "biller_code", nullable = false, length = 20)
    private String billerCode;

    @Column(name = "mobile_phone", length = 20)
    private String mobilePhone;

    @Column(name = "channel_code", length = 20)
    private String channelCode;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "request_data", nullable = false)
    private String requestData;

    @Lob
    @Column(name = "response_data")
    @Type(type = "org.hibernate.type.TextType")
    private String responseData;

    @Column(name = "request_date", nullable = false)
    @JsonFormat(pattern="dd/MM/yyyy HH:mm:ss")
    private Date requestDate;

    @Column(name = "processing_duration", nullable = false)
    private Integer processingDuration;
}