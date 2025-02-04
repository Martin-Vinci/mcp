package com.greybox.mediums.repository;

import com.greybox.mediums.entities.TransBatch;
import com.greybox.mediums.entities.TransBatchItem;
import com.greybox.mediums.entities.TransactionRef;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransBatchRepo extends CrudRepository<TransBatch, Long> {

    @Query(value = "SELECT tb.* FROM {h-schema}trans_batch tb WHERE tb.initiator_phone = ?1", nativeQuery = true)
    List<TransBatch> findBatchByPhone(String phoneNo);

    @Query(value = "select u.* from  {h-schema}trans_batch u where u.batch_id = ?1", nativeQuery = true)
    TransBatch getById(Long batchId);

    @Query(value = "SELECT tb.* FROM {h-schema}trans_batch tb WHERE tb.item_uuid = ?1", nativeQuery = true)
    TransBatch findByBatchUuid(String itemUuid);
}