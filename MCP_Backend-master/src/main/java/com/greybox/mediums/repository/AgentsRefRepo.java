package com.greybox.mediums.repository;

import com.greybox.mediums.entities.AgentsRef;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AgentsRefRepo extends CrudRepository<AgentsRef, Integer> {
    @Query(value = "select u.* from {h-schema}agents_ref u order by u.agent_code", nativeQuery = true)
    List<AgentsRef> findAgents();

    @Query(value = "select max(u.agent_code) from {h-schema}agents_ref u", nativeQuery = true)
    Integer findMaxAgentCode();
}