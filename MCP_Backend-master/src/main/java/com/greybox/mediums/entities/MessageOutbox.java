package com.greybox.mediums.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "message_outbox")
@Entity
@Data
public class MessageOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer messageId;

    @Column(name = "message_text", nullable = false)
    private String messageText;

    @Column(name = "time_generated", nullable = false, updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeGenerated;

    @Column(name = "time_sent")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeSent;

    @Column(name = "recipient_number", nullable = false)
    private String recipientNumber;

    @Column(name = "message_status", nullable = false)
    private String messageStatus;

    @Column(name = "flash_message", nullable = false)
    private Boolean flashMessage = false;

    @Column(name = "email_message")
    private Boolean emailMessage = false;

    @Column(name = "email_subject")
    private String emailSubject;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "email_attachment")
    private String emailAttachment;

    @Transient
    private boolean deliverSMS;



}