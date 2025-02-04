package com.greybox.mediums.repository;

import com.greybox.mediums.entities.SmsAlert;
import org.springframework.data.repository.CrudRepository;

public interface SmsAlertRepo extends CrudRepository<SmsAlert, Long> {
}