package com.greybox.mediums.repository;

import com.greybox.mediums.entities.ServicePostingDetail;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ServicePostingDetailRepo extends CrudRepository<ServicePostingDetail, Integer> {
    @Query(value = "select u.* from  {h-schema}service_posting_details u where u.service_id = :service_id order by u.posting_priority", nativeQuery = true)
    List<ServicePostingDetail> findServicePostingPolicies(@Param("service_id") Integer paramInteger);

    @Modifying
    @Query(value = "delete from {h-schema}service_posting_details where posting_detail_id = ?1", nativeQuery = true)
    void deleteServicePostingDetail(Integer paramInteger);
}
