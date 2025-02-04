package com.greybox.mediums.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "agent_company_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentCompanyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Integer companyId;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;
}
