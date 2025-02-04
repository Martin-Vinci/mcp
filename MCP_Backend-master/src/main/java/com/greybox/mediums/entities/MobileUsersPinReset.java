package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "mobile_users_pin_reset")
@Entity
@Data
public class MobileUsersPinReset {
    @Id
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "pin_delivered", nullable = false, length = 1)
    private String pinDelivered;

}