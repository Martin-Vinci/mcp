package com.greybox.mediums.repository;

import com.greybox.mediums.entities.CustomerRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerRefRepo extends JpaRepository<CustomerRef, Integer> {
    @Query(value = "select u.* from {h-schema}customer_ref u order by u.customer_name", nativeQuery = true)
    List<CustomerRef> findCustomers();
}