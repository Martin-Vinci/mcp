package com.greybox.mediums.repository.mobile_loan;


import com.greybox.mediums.entities.mobile_loan.LnCustomer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepo extends CrudRepository<LnCustomer, Integer> {
    @Query(value = "select * from  {h-schema}mbl_customer u order by u.cust_id desc", nativeQuery = true)
    List<LnCustomer> findAll();

    @Query(value = "select * from  {h-schema}mbl_customer u where u.cust_id = :cust_id", nativeQuery = true)
    LnCustomer findCustById(@Param("cust_id") Integer custId);

    @Query(value = "select u.* from  {h-schema}mbl_customer u where u.phone_no = :phone_no and trim(u.password) = :password order by first_name", nativeQuery = true)
    LnCustomer findCustomerByPhoneAndPin(
            @Param("phone_no") String phoneNo, @Param("password") String pinNumber
    );

    @Query(value = "select u.* from  {h-schema}mbl_customer u where u.phone_no = :phone_no order by first_name", nativeQuery = true)
    LnCustomer findCustomerByPhone(
            @Param("phone_no") String phoneNo);
}