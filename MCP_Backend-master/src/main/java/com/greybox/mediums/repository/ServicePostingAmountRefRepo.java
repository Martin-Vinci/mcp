package com.greybox.mediums.repository;

import com.greybox.mediums.entities.ServicePostingAmountRef;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ServicePostingAmountRefRepo extends CrudRepository<ServicePostingAmountRef, Integer> {
    @Query(value = "select u.* from {h-schema}service_posting_amount_ref u order by amount_type", nativeQuery = true)
    List<ServicePostingAmountRef> findPostingPolicyAmountTypes();
}