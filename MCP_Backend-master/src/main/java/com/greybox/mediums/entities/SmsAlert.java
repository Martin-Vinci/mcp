package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Table(name = "sms_alerts")
@Entity
@Data
public class SmsAlert {
    @Column(name = "message_id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @Column(name = "phone_no", nullable = false, length = 20)
    private String phoneNo;

    @Lob
    @Column(name = "message_body", nullable = false)
    private String messageBody;

    @Column(name = "sent_flag", nullable = false, length = 2)
    private String sentFlag;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "sent_feedback", length = 100)
    private String sentFeedback;

}