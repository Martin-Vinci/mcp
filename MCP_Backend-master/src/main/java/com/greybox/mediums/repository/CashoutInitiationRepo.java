package com.greybox.mediums.repository;

import com.greybox.mediums.entities.CashoutInitiation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface CashoutInitiationRepo extends CrudRepository<CashoutInitiation, Integer> {
    @Query(value = "select u.* from  {h-schema}cashout_initiations u where customer_phone = :customer_phone and u.withdraw_code = :withdraw_code and approved IS NOT TRUE", nativeQuery = true)
    CashoutInitiation findWithdrawCodeDetails(
            @Param("withdraw_code") String withdrawCode,
            @Param("customer_phone") String acctNo
    );
}