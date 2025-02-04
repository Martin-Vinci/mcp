package com.greybox.mediums.repository;

import com.greybox.mediums.entities.CustomerDetail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CustomerDetailRepo extends CrudRepository<CustomerDetail, Integer> {

    @Query(value = "select u.* from  {h-schema}customer_details u where u.user_id = ?1", nativeQuery = true)
    CustomerDetail findCustomerDetails(long userId);
}