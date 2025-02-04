package com.greybox.mediums.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDate;

@Table(name = "user_type_ref")
@Entity
@Data
public class UserType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_type_id", nullable = false)
    private Integer userTypeId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "create_dt", nullable = false, updatable = false)
    @JsonFormat(pattern="dd-MM-yyyy")
    private LocalDate createDt;

    @Column(name = "modified_by", insertable = false)
    private String modifiedBy;

    @Column(name = "modify_dt", insertable = false)
    private LocalDate modifyDt;

}