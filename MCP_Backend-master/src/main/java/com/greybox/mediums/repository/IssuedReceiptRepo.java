package com.greybox.mediums.repository;
import com.greybox.mediums.entities.IssuedReceipt;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface IssuedReceiptRepo extends CrudRepository<IssuedReceipt, Integer> {
    @Query(value = "select * from {h-schema}issued_receipts T1 where T1.outlet_code = ?1 and T1.txn_id = ?2 order by receipt_id", nativeQuery = true)
    List<IssuedReceipt> findIssuedReceipts(String outletCode, Integer txnId);

    @Query(value = "select * from {h-schema}issued_receipts T1 where T1.outlet_code = ?1 " +
            " and DATE(T1.date_created) >= ?2 " +
            " and DATE(T1.date_created) <= ?3 order by receipt_id", nativeQuery = true)
    List<IssuedReceipt> findIssuedReceipts(String outletCode, LocalDate fromDate, LocalDate toDate);
}