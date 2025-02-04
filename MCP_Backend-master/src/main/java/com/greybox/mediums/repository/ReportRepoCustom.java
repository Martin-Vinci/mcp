package com.greybox.mediums.repository;


import com.greybox.mediums.entities.*;
import com.greybox.mediums.models.DashboardActiveOutlet;
import com.greybox.mediums.models.SearchCriteria;
import com.greybox.mediums.models.TransactionBand;
import com.greybox.mediums.models.TransactionSummary;

import java.math.BigDecimal;
import java.util.List;


public interface ReportRepoCustom {
    List<TransactionRef> findTransactions(TransactionRef paramTransactionRef);

    List<BillerNotif> findBillerNotifications(BillerNotif paramBillerNotif);

    List<MobileUser> findActiveAgents(MobileUser paramMobileUser);

    List<TransactionBand> findTransactionBands(TransactionBand paramTransactionBand);

    List<TransactionSummary> findTransactionSummary(SearchCriteria paramSearchCriteria);

    List<MobileUserAccount> findUserBalances(SearchCriteria paramSearchCriteria);

    List<DashboardActiveOutlet> findActiveAgentsByTransactions(SearchCriteria paramSearchCriteria);

    Integer findNewUsers(SearchCriteria paramSearchCriteria);

    BigDecimal findBankCommission(SearchCriteria paramSearchCriteria);

    BigDecimal findBankExpenses(SearchCriteria paramSearchCriteria);

    List<MobileUser> findAgentDetails(MobileUser paramMobileUser);

    List<TransactionDetail> findPendingTransactionsWithSuccessFlagY();

    List<MobileUser> findCustomers(MobileUser request);
    List<TransactionVoucher> findTransactionVouchers(TransactionVoucher request);
    List<User> findUsers(User request);
    List<MobileUser> findAgentWithHoldingTax(MobileUser request);
    public List<TransBatch> findTransBatchByPhone(String phone);
}

