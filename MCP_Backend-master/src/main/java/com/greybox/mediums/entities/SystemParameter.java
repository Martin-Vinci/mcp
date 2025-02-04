package com.greybox.mediums.entities;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Table(name = "system_parameter", indexes = {
        @Index(name = "system_parameter_param_cd_idx", columnList = "param_cd")
})
@Entity
@Data
public class SystemParameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "param_id", nullable = false)
    private Integer paramId;

    @Column(name = "param_cd", nullable = false, length = 4)
    private String paramCode;

    @Column(name = "param_value", nullable = false, length = 30)
    private String paramValue;

    @Column(name = "param_descr", nullable = false, length = 50)
    private String paramDescr;

    @Column(name = "editable", length = 1)
    private String editable;

    @Column(name = "status", length = 10)
    private String status;

    @Column(name = "modify_date")
    private LocalDate modifyDate;

    @Column(name = "modified_by", length = 10)
    private String modifiedBy;

}