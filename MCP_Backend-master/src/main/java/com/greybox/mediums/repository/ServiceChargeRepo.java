package com.greybox.mediums.repository;

import com.greybox.mediums.entities.ServiceCharge;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ServiceChargeRepo extends CrudRepository<ServiceCharge, Integer> {
    @Query(value = "select u.* from  {h-schema}service_charge u where u.service_id = :service_id", nativeQuery = true)
    ServiceCharge findServiceCharge(@Param("service_id") Integer serviceId);
}