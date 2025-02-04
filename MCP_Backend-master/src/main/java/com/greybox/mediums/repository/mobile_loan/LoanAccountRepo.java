package com.greybox.mediums.repository.mobile_loan;

import com.greybox.mediums.entities.mobile_loan.LnAccount;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoanAccountRepo extends CrudRepository<LnAccount, Integer> {
    @Query(value = "select * from  {h-schema}loan_account u where u.credit_appl_id in (select credit_app_id from  {h-schema}mbl_credit_app where cust_id = :cust_id)", nativeQuery = true)
    List<LnAccount> findAll(@Param("cust_id") Integer custId);

    @Query(value = "select * from  {h-schema}loan_account u where u.status = :status", nativeQuery = true)
    List<LnAccount> findLoansByStatus(@Param("status") String status);

    @Query(value = "select * from  {h-schema}loan_account u where u.loan_number = :loan_number", nativeQuery = true)
    LnAccount findLoanAccount(@Param("loan_number") String loanNumber);

    @Query(value = "select * from  {h-schema}loan_account u where u.loan_id = ?1", nativeQuery = true)
    LnAccount findLoanAccount(Integer loanId);

    @Query(value = "select * from  {h-schema}loan_account u order by u.loan_id desc", nativeQuery = true)
    List<LnAccount> findAll();
}