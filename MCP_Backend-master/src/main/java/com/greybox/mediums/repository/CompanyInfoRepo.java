package com.greybox.mediums.repository;

import com.greybox.mediums.entities.AgentCompanyInfo;
import com.greybox.mediums.entities.Biller;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanyInfoRepo extends CrudRepository<AgentCompanyInfo, Integer> {
    @Query(value = "select u.* from  {h-schema}agent_company_info u", nativeQuery = true)
    List<AgentCompanyInfo> findAgentCompanyInfo();
}
