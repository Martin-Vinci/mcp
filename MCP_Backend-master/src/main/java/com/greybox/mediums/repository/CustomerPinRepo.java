package com.greybox.mediums.repository;

import com.greybox.mediums.entities.CustomerPin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPinRepo extends JpaRepository<CustomerPin, Long> {
}