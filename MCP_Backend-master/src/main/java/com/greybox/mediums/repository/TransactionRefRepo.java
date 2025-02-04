package com.greybox.mediums.repository;

import com.greybox.mediums.entities.TransactionRef;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface TransactionRefRepo extends CrudRepository<TransactionRef, Integer>, TransactionRefCustom {
    @Query(value = "select u.* from  {h-schema}transaction_ref u where u.trans_id = :trans_id", nativeQuery = true)
    TransactionRef findTransactionRef(@Param("trans_id") Long paramLong);


}