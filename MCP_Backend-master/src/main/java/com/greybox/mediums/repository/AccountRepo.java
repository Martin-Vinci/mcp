package com.greybox.mediums.repository;

import com.greybox.mediums.entities.Account;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountRepo extends CrudRepository<Account, Integer> {
    @Query(value = "select u.* from  {h-schema}accounts u where u.agent_id = :agent_id order by u.acct_no", nativeQuery = true)
    List<Account> findAgentAccountsByAgentId(
            @Param("agent_id") Integer agentId
    );

    @Query(value = "select u.* from  {h-schema}accounts u where u.entity_id = :entity_id and u.entity_code = :entity_code order by u.acct_no", nativeQuery = true)
    List<Account> findAccountsByEntityId(
            @Param("entity_id") Integer agentId,
            @Param("entity_code") String entityCode
    );

}