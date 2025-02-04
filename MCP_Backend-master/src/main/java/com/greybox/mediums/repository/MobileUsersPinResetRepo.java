package com.greybox.mediums.repository;

import com.greybox.mediums.entities.MobileUsersPinReset;
import com.greybox.mediums.entities.ServiceCommission;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;

public interface MobileUsersPinResetRepo extends CrudRepository<MobileUsersPinReset, String> {
    @Modifying
    @Query(value = "update {h-schema}mobile_users_pin_reset set pin_delivered = 'Y' where phone_number = :phoneNo", nativeQuery = true)
    void updateSentResetPin(@Param("phoneNo") String phoneNo);

    @Query(value = "select u.* from  {h-schema}mobile_users_pin_reset u where u.pin_delivered <> 'Y' and u.phone_number = :phone_number", nativeQuery = true)
    MobileUsersPinReset findMobileUser(
            @Param("phone_number") String phoneNo
    );


}