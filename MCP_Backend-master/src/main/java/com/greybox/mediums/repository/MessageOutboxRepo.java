package com.greybox.mediums.repository;

import com.greybox.mediums.entities.MessageOutbox;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.sql.Date;
import java.util.List;

public interface MessageOutboxRepo extends CrudRepository<MessageOutbox, Integer> {

    @Query(value = "select u.* from  {h-schema}message_outbox u where DATE(u.time_generated) >= :startDate and  DATE(u.time_generated) <= :endDate order by id desc", nativeQuery = true)
    List<MessageOutbox> findSMSByDateRange(@Param("startDate") Date startDate, @Param("endDate")Date endDate);

    @Query(value = "select u.* from  {h-schema}message_outbox u where  DATE(u.time_generated) >= :startDate and  DATE(u.time_generated) <= :endDate and u.recipient_number = :recipient_number order by id desc", nativeQuery = true)
    List<MessageOutbox> findSMSByPhone(@Param("startDate") Date startDate,
                                       @Param("endDate")Date endDate,
                                       @Param("recipient_number") String phoneNo);

    @Query(value = "select u.* from  {h-schema}message_outbox u where u.recipient_number = :recipient_number order by id asc", nativeQuery = true)
    List<MessageOutbox> findMessages(@Param("recipient_number") String phoneNo);
}