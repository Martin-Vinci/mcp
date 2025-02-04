package com.greybox.mediums.repository;

import com.greybox.mediums.entities.AgentOutlet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AgentOutletRepo extends JpaRepository<AgentOutlet, Integer> {
    @Query(value = "select u.* from  {h-schema}agent_outlets u where u.agent_id = :agent_id order by u.outlet_no", nativeQuery = true)
    List<AgentOutlet> findOutlets(
            @Param("agent_id") Integer agentId
    );

    @Query(value = "select count(*) from  {h-schema}agent_outlets u where u.agent_id = :agent_id", nativeQuery = true)
    Integer findOutletCount(
            @Param("agent_id") Integer agentId
    );

    @Query(value = "select agent_code from  {h-schema}agents_ref u where u.agent_id = :agent_id", nativeQuery = true)
    Integer findAssociatedAgentCode(
            @Param("agent_id") Integer agentId
    );

}