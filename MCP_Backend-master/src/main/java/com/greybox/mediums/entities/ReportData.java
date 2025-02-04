package com.greybox.mediums.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = false)
@Table(name = "entities")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
}
