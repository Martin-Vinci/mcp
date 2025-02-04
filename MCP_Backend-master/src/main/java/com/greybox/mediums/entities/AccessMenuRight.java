package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.*;

@Table(name = "access_menu_rights")
@Entity
@Data
public class AccessMenuRight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_right_id", nullable = false)
    private Integer accessRightId;

    @Column(name = "user_type_id", nullable = false)
    private Integer userTypeId;

    @Column(name = "menu_id", nullable = false)
    private Integer menuId;

    @Transient
    private String accessCategory;
}