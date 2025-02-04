package com.greybox.mediums.repository;

import com.greybox.mediums.entities.TransactionDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TransactionDetailRepo extends JpaRepository<TransactionDetail, Long> {
    @Query(value = "select u.* from  {h-schema}transaction_details u where u.main_trans_id = ?1 order by u.trans_id", nativeQuery = true)
    List<TransactionDetail> findTransactionDetailReport(Long paramLong);

    @Query(value = "select u.* from  {h-schema}transaction_details u where u.main_trans_id = ?1 and status = 'Posted' order by u.trans_id", nativeQuery = true)
    List<TransactionDetail> findPostedTransactionDetails(Long paramLong);

    @Query(value = "select u.* from  {h-schema}transaction_details u where u.main_trans_id = ?1 and status <> 'Posted' order by u.trans_id", nativeQuery = true)
    List<TransactionDetail> findPendingTransactionDetails(Long transId);




//    @Query(value = "SELECT tr.initiator_phone_no, td.* FROM {h-schema}transaction_ref tr, {h-schema}transaction_details td " +
//            "WHERE td.main_trans_id = tr.trans_id " +
//            "AND tr.success_flag = 'Y' " +
//            "AND td.status = 'Pending' " +
//            "AND FLOOR(EXTRACT(EPOCH FROM (current_timestamp - td.posting_dt)) / 60) >= 10 LIMIT 100", nativeQuery = true)
//    List<TransactionDetail> findPendingTransactionsWithSuccessFlagY();
}