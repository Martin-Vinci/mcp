package com.greybox.mediums.repository.mobile_loan;

import com.greybox.mediums.entities.mobile_loan.LnCreditApp;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CreditAppRepoCustom {
    List<LnCreditApp> findCreditAppl(LnCreditApp request);
}