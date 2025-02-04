package com.greybox.mediums.repository;

import com.greybox.mediums.entities.ServiceCommission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ServiceCommissionRepo extends CrudRepository<ServiceCommission, Integer> {

    @Query(value = "select u.* from  {h-schema}service_commission u where u.service_id = :service_id", nativeQuery = true)
    ServiceCommission findServiceCommission(
            @Param("service_id") Integer serviceId
    );
}