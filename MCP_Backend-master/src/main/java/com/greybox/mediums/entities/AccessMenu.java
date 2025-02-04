package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "access_menu_ref")
@Entity
@Data
public class AccessMenu {
    @Id
    @Column(name = "menu_id", nullable = false)
    private Integer menuId;

    @Column(name = "description", nullable = false, length = 100)
    private String description;

    @Column(name = "menu_code", nullable = false, length = 80)
    private String menuCode;

    @Column(name = "access_scope", nullable = false, length = 7)
    private String accessScope;

}