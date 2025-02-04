package com.greybox.mediums.repository;

import com.greybox.mediums.entities.ServiceChargeTier;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceChargeTierRepo extends CrudRepository<ServiceChargeTier, Integer> {

    @Query(value = "select u.* from {h-schema}service_charge_tiers u where u.charge_id = :charge_id order by u.tier_no", nativeQuery = true)
    List<ServiceChargeTier> findChargeTiers(@Param("charge_id") Integer chargeId);

    @Modifying
    @Query(value = "delete from {h-schema}service_charge_tiers u where u.charge_id = :charge_id", nativeQuery = true)
    void deleteCurrentTierRecords(@Param("charge_id") Integer chargeId);
}