package com.greybox.mediums.repository.mobile_loan;

import com.greybox.mediums.entities.mobile_loan.LnSchedule;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoanScheduleRepo extends CrudRepository<LnSchedule, Integer> {
    @Query(value = "select * from  {h-schema}loan_schedule u where u.loan_id = :loan_id order by u.schedule_no", nativeQuery = true)
    List<LnSchedule> findAll(@Param("loan_id") Integer loanId);

    @Query(value = "select * from  {h-schema}loan_schedule u where loan_id = :loan_id and loan_schedule_id in (select min(loan_schedule_id) from  {h-schema}loan_schedule ls where loan_id = :loan_id and status in ('NOT_PAID','PARTIALLY_PAID'))", nativeQuery = true)
    LnSchedule findMinimumUnPaidSchedule(@Param("loan_id") Integer loanId);

    @Query(value = "select * from  {h-schema}loan_schedule u where u.loan_id = :loan_id and  status in ('NOT_PAID', 'PARTIALLY_PAID') order by u.schedule_no", nativeQuery = true)
    List<LnSchedule> findPendingSchedules(@Param("loan_id") Integer loanId);
}