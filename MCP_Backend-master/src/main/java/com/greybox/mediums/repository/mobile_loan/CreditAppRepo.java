package com.greybox.mediums.repository.mobile_loan;

import com.greybox.mediums.entities.mobile_loan.LnCreditApp;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CreditAppRepo extends CrudRepository<LnCreditApp, Integer>, CreditAppRepoCustom {
    @Query(value = "select * from  {h-schema}mbl_credit_app u where u.cust_id = :cust_id", nativeQuery = true)
    List<LnCreditApp> findAll(@Param("cust_id") Integer custId);

    @Query(value = "select * from  {h-schema}mbl_credit_app u where u.credit_app_id = ?1", nativeQuery = true)
    LnCreditApp findCreditAppl(Integer creditApplId);
}