package com.greybox.mediums.repository;

import com.greybox.mediums.entities.MobileUser;
import com.greybox.mediums.entities.ServicePostingAccountsRef;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ServicePostingAccountsRefRepo extends CrudRepository<ServicePostingAccountsRef, Integer> {
    @Query(value = "select u.* from {h-schema}service_posting_accounts_ref u order by acct_type", nativeQuery = true)
    List<ServicePostingAccountsRef> findPostingPolicyAccountTypes();
}