package com.greybox.mediums.repository;

import com.greybox.mediums.entities.TransCenteTrust;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface TransCenteTrustRepo extends CrudRepository<TransCenteTrust, Integer>, TransCenteTrustRepoCustom {
    @Query(value = "select u.* from  {h-schema}trans_cente_trust u where batch_id = ?1 order by trans_id desc", nativeQuery = true)
    List<TransCenteTrust> findTransCenteTrust(Long paramLong);

    @Query(value = "select u.* from  {h-schema}trans_cente_trust u, {h-schema}transaction_ref t where t.trans_id = u.main_trans_id and t.service_code = ?2 and batch_id = ?1 order by trans_id desc", nativeQuery = true)
    List<TransCenteTrust> findTransCenteTrust(Long paramLong, Integer paramInteger);
}
