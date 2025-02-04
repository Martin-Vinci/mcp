package com.greybox.mediums.repository;

import com.greybox.mediums.entities.CashoutInitiation;
import com.greybox.mediums.entities.OutletWithdrawInitiation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutletWithdrawInitiationRepo extends CrudRepository<OutletWithdrawInitiation, Integer> {
    @Query(value = "select u.* from  {h-schema}mobile_users u where  u.acct_type = 'CUSTOMER'", nativeQuery = true)
    List<OutletWithdrawInitiation> findOutletWithdrawOTP();

    @Query(value = "select u.* from  {h-schema}outlet_withdraw_initiations u where u.withdraw_code = :withdraw_code and outlet_code = :outlet_code and approved IS NOT TRUE", nativeQuery = true)
    OutletWithdrawInitiation findOutletWithdrawCodeDetails(
            @Param("withdraw_code") String withdrawCode,
            @Param("outlet_code") String outletCode
    );




}