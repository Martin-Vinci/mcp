package com.greybox.mediums.repository.mobile_loan;

import com.greybox.mediums.entities.mobile_loan.LoanRepaymentHistory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface LoanRepaymentRepo extends CrudRepository<LoanRepaymentHistory, Integer> {

    @Query(value = "select * from  {h-schema}loan_repayment_history u order by u.trans_id desc", nativeQuery = true)
    List<LoanRepaymentHistory> findAll();
}