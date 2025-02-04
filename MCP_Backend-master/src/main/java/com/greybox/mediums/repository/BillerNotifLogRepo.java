package com.greybox.mediums.repository;

import com.greybox.mediums.entities.BillerNotifLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BillerNotifLogRepo extends CrudRepository<BillerNotifLog, Integer> {
    @Query(value = "select u.* from  {h-schema}biller_notif_log u order by u.notif_log_id desc", nativeQuery = true)
    List<BillerNotifLog> findBillPaymentLog();
}