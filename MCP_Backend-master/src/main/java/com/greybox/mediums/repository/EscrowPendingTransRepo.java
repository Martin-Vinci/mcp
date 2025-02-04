package com.greybox.mediums.repository;

import com.greybox.mediums.entities.EscrowPendingTrans;
import com.greybox.mediums.entities.TransactionRef;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EscrowPendingTransRepo extends CrudRepository<EscrowPendingTrans, Integer> {
    @Query(value = "select u.* from  {h-schema}escrow_pending_trans u where u.util_posted <> 'Y' order by u.trans_id desc", nativeQuery = true)
    List<EscrowPendingTrans> findTransactions();
}