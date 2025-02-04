package com.greybox.mediums.repository;

import com.greybox.mediums.entities.ServiceCommissionTier;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceCommissionTierRepo extends CrudRepository<ServiceCommissionTier, Integer> {
    @Query(value = "select u.* from {h-schema}service_commission_tiers u where u.commission_id = :commission_id order by u.tier_no", nativeQuery = true)
    List<ServiceCommissionTier> findCommissionTiers(@Param("commission_id") Integer commissionId);

    @Modifying
    @Query(value = "delete from {h-schema}service_commission_tiers u where u.commission_id = :commission_id", nativeQuery = true)
    void deleteCurrentTierRecords(@Param("commission_id") Integer commissionId);
}