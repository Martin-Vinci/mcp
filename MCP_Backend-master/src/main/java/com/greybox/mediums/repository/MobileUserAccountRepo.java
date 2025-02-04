package com.greybox.mediums.repository;

import com.greybox.mediums.entities.MobileUserAccount;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface MobileUserAccountRepo extends CrudRepository<MobileUserAccount, Integer>, MobileUserAccountRepoCustom {

    @Query(value = "select u.* from  {h-schema}mobile_user_accounts u where u.user_id = :user_id  and u.account_type  <> 'COMMISSION'", nativeQuery = true)
    List<MobileUserAccount> findUserTransactingAccounts(@Param("user_id") long paramLong);

    @Query(value = "select u.* from  {h-schema}mobile_user_accounts u where u.user_id = :user_id  and u.active IS TRUE", nativeQuery = true)
    List<MobileUserAccount> findMobileUserAcct(@Param("user_id") long paramLong);

    @Query(value = "select u.* from  {h-schema}mobile_user_accounts u where u.balance_update_dt is null and u.active IS TRUE", nativeQuery = true)
    List<MobileUserAccount> findAccountsForBalanceUpdate();

    @Query(value = "select T2.* from {h-schema}mobile_users T1, {h-schema}mobile_user_accounts T2 where T1.id = T2.user_id and T1.entity_code in (select entity_code from {h-schema}mobile_users T1 where T1.outlet_code = :outlet_code) and T2.account_type  = 'COMMISSION'", nativeQuery = true)
    MobileUserAccount findAgentCommissionAccount(@Param("outlet_code") String paramString);

    @Query(value = "select T2.* from {h-schema}mobile_user_accounts T2 where T2.account = :account", nativeQuery = true)
    MobileUserAccount findAccountDetails(@Param("account") String paramString);

    @Query(value = "SELECT mua.* FROM {h-schema}mobile_users mu " +
            "JOIN {h-schema}mobile_user_accounts mua ON mu.id = mua.user_id " +
            "WHERE mu.phone_number = ?1", nativeQuery = true)
    MobileUserAccount findUserAccountByPhoneNumber(String phoneNumber);

    @Query(value = "SELECT mua.account FROM {h-schema}mobile_users mu " +
            "JOIN {h-schema}mobile_user_accounts mua ON mu.id = mua.user_id " +
            "WHERE mu.phone_number = ?1", nativeQuery = true)
    String findAccountNumberByPhoneNumber(String phoneNumber);

    @Modifying
    @Query(value = "UPDATE {h-schema}mobile_user_accounts SET last_activity_date=current_timestamp WHERE account= ?1", nativeQuery = true)
    void updateAccountLastActivityDate(String paramString);

    @Query(value = "SELECT mu.phone_number " +
            "FROM mobile_users mu, transaction_ref tr " +
            "WHERE mu.outlet_code = tr.posted_by " +
            "AND tr.trans_id = ?1", nativeQuery = true)
    String findPhoneNumberByTransactionId(Long transId);

    @Query(value = "SELECT mu.company_id " +
                    "FROM {h-schema}mobile_users mu, {h-schema}mobile_user_accounts mua " +
                    "WHERE mu.id = mua.user_id " +
                    "AND mu.acct_type NOT IN ('CUSTOMER', 'AGENT') " +
                    "AND mua.account = ?1",
            nativeQuery = true
    )
    Integer findCompanyIdByAccount(String account);
}
