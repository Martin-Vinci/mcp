package com.greybox.mediums.repository;

import com.greybox.mediums.entities.Biller;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BillerRepo extends CrudRepository<Biller, Integer> {
    @Query(value = "select u.* from  {h-schema}biller_ref u order by u.biller_code desc", nativeQuery = true)
    List<Biller> findBillers();

    @Query(value = "SELECT * from {h-schema}biller_ref br where status = 'ACTIVE' and DATE_PART('day', (current_date - coalesce(last_update_date, current_date - 1))) > 0 ", nativeQuery = true)
    List<Biller> findBillerForPriceUpdate();

    @Query(value = "select u.* from  {h-schema}biller_ref u where u.biller_code = :biller_code order by u.biller_code desc", nativeQuery = true)
    Biller findBillerByBillerCode(@Param("biller_code") String billerCode);
}
