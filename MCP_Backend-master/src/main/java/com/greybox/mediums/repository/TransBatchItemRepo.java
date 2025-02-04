package com.greybox.mediums.repository;

import com.greybox.mediums.entities.TransBatch;
import com.greybox.mediums.entities.TransBatchItem;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface TransBatchItemRepo extends CrudRepository<TransBatchItem, Long> {
    @Query(value = "select u.* from  {h-schema}trans_batch_item u where u.batch_item_id = ?1", nativeQuery = true)
    TransBatchItem getById(Long batchItemId);

    @Query(value = "SELECT a.* FROM {h-schema}trans_batch_item a, {h-schema}trans_batch tb WHERE a.batch_id = tb.batch_id AND tb.item_uuid = ?1", nativeQuery = true)
    List<TransBatchItem> findByItemUuid(String itemUuid);

    @Modifying
    @Query(value = "UPDATE {h-schema}trans_batch_item SET status = ?1, message = ?2 WHERE batch_item_id = ?3", nativeQuery = true)
    void updateTransBatchItem(String status, String message, Long batchItemId);

}