package com.greybox.mediums.repository;

import com.greybox.mediums.entities.ServiceRef;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceRefRepo extends CrudRepository<ServiceRef, Integer> {
    @Query(value = "select u.* from {h-schema}service_ref u order by u.service_code", nativeQuery = true)
    List<ServiceRef> findServices();

    @Query(value = "select u.* from {h-schema}service_ref u where  u.service_code = :service_code order by u.service_code", nativeQuery = true)
    ServiceRef findServiceByCode(@Param("service_code") Integer serviceCode);
}