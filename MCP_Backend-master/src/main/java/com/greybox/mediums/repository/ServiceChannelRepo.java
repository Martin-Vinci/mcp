package com.greybox.mediums.repository;

import com.greybox.mediums.entities.ServiceChannel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ServiceChannelRepo extends CrudRepository<ServiceChannel, Integer> {

    @Query(value = "select u.* from  {h-schema}service_channel u order by u.description", nativeQuery = true)
    List<ServiceChannel> findServiceChannels();

    @Query(value = "select u.* from  {h-schema}service_channel u where u.channel_code = ?1", nativeQuery = true)
    ServiceChannel findServiceChannelByCode(String channelCode);

}