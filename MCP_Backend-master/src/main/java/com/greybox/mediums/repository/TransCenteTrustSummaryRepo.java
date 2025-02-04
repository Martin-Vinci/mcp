package com.greybox.mediums.repository;

import com.greybox.mediums.entities.TransCenteTrustSummary;
import java.sql.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface TransCenteTrustSummaryRepo  extends CrudRepository<TransCenteTrustSummary, Long> {
    @Query(value = "select u.* from  {h-schema}trans_cente_trust_summary u where DATE(u.posting_date) >= :startDate and  DATE(u.posting_date) <= :endDate order by batch_id desc", nativeQuery = true)
    List<TransCenteTrustSummary> findTransCenteTrust(@Param("startDate") Date paramDate1, @Param("endDate") Date paramDate2);
}
