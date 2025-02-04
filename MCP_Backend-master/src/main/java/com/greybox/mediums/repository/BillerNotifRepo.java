package com.greybox.mediums.repository;

import com.greybox.mediums.entities.BillerNotif;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface BillerNotifRepo extends CrudRepository<BillerNotif, Integer> {

    @Query(value = "select u.* from  {h-schema}biller_notif u where u.biller_code = ?1 and u.channel_code = ?2 and u.third_party_reference = ?3 order by u.biller_notif_id desc", nativeQuery = true)
    BillerNotif findBillerNotificationByReferenceNo(String billerCode, String sourceCode, String thirdPartyReference);

    @Modifying
    @Query(value = "UPDATE {h-schema}biller_notif set status = ?2, reversal_reason = ?3, trans_id_ext = ?4 WHERE biller_notif_id = ?1", nativeQuery = true)
    void updateBillNotifiation(Integer notificationId, String status, String reason, String externaTransRef);
}