package com.greybox.mediums.repository;

import com.greybox.mediums.entities.MobileUser;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface MobileUserRepo extends CrudRepository<MobileUser, Integer> {
    @Query(value = "select u.* from  {h-schema}mobile_users u where u.acct_type = 'CUSTOMER'", nativeQuery = true)
    List<MobileUser> findMobileUser();

    @Query(value = "select u.* from {h-schema}mobile_users u where u.acct_type = 'OUTLET' and u.outlet_code is not null order by u.customer_name", nativeQuery = true)
    List<MobileUser> findOutlets();

    @Query(value = "select u.* from {h-schema}mobile_users u where u.acct_type IN ('AGENT', 'SUPER_AGENT')", nativeQuery = true)
    List<MobileUser> findAgents();

    @Query(value = "select u.* from {h-schema}mobile_users u where u.phone_number = :phone_number and u.acct_type IN ('AGENT', 'SUPER_AGENT')", nativeQuery = true)
    List<MobileUser> findEntityAgentsByPhone(@Param("phone_number") String paramString);

    @Query(value = "select u.* from {h-schema}mobile_users u where LOWER(u.customer_name) LIKE LOWER(CONCAT('%', ?1,'%')) and u.acct_type IN ('AGENT', 'SUPER_AGENT')", nativeQuery = true)
    List<MobileUser> findEntityAgentsByName(String paramString);

    @Query(value = "select u.* from {h-schema}mobile_users u where  u.entity_code = :entity_code and u.acct_type = 'OUTLET'", nativeQuery = true)
    List<MobileUser> findOutlets(@Param("entity_code") Integer paramInteger);

    @Query(value = "select u.* from {h-schema}mobile_users u where  u.approval_status = false and u.acct_type <> 'AGENT'", nativeQuery = true)
    List<MobileUser> findPendingCustomers();

    @Query(value = "select u.* from {h-schema}mobile_users u where  u.outlet_code = :outlet_code and u.acct_type = 'OUTLET'", nativeQuery = true)
    MobileUser findOutletByOutletCode(@Param("outlet_code") String paramString);

    @Query(value = "select u.* from {h-schema}mobile_users u where  u.outlet_code = :outlet_code and u.acct_type = 'SUPER_AGENT'", nativeQuery = true)
    MobileUser findSuperAgentByOutletCode(@Param("outlet_code") String paramString);

    @Query(value = "select u.* from {h-schema}mobile_users u where u.phone_number = ?1 and u.acct_type <> 'AGENT'", nativeQuery = true)
    MobileUser findMobileUserByPhoneNo(String paramString);

    @Query(value = "select u.* from {h-schema}mobile_users u where u.phone_number = ?1 and u.wap_otp = ?2", nativeQuery = true)
    MobileUser findMobileUserByPhoneNo(String paramString1, String paramString2);

    @Query(value = "select u.* from {h-schema}mobile_users u where u.phone_number = :phone_number and u.acct_type IN ('CUSTOMER')", nativeQuery = true)
    List<MobileUser> findCustomersByPhone(@Param("phone_number") String paramString);

    @Query(value = "select u.* from {h-schema}mobile_users u where u.acct_type IN ('CUSTOMER')", nativeQuery = true)
    List<MobileUser> findActiveCustomers();

    @Query(value = "select u.* from {h-schema}mobile_users u where u.acct_type IN ('CUSTOMER') and u.gender = ?1", nativeQuery = true)
    List<MobileUser> findActiveCustomerByGender(String paramString);

    @Query(value = "select u.* from {h-schema}mobile_users u where u.acct_type IN ('CUSTOMER') and u.gender Not In ('F', 'M')", nativeQuery = true)
    List<MobileUser> findActiveCustomerWithoutGender();

    @Query(value = "select u.* from {h-schema}mobile_users u where LOWER(u.customer_name) LIKE LOWER(CONCAT('%', ?1,'%')) and u.acct_type IN ('CUSTOMER')", nativeQuery = true)
    List<MobileUser> findEntityCustomersByName(String paramString);

    @Query(value = "select u.* from {h-schema}mobile_users u where LOWER(trim(u.customer_name)) LIKE LOWER(CONCAT('%', ?2,'%')) and u.phone_number = ?1 u.acct_type IN ('CUSTOMER')", nativeQuery = true)
    List<MobileUser> findEntityCustomersByPhoneAndName(String paramString1, String paramString2);

    @Query(value = "select max(u.entity_code) from {h-schema}mobile_users u where u.acct_type IN ('AGENT', 'SUPER_AGENT')", nativeQuery = true)
    Integer findMaxAgentCode();

    @Modifying
    @Query(value = "update {h-schema}mobile_users set pin = :pinNo, pin_change_flag = false, locked_flag = false, failed_login_count = 0, wap_otp_expiry = :expiryTime where phone_number = :phoneNo", nativeQuery = true)
    void resetCustomerPin(@Param("phoneNo") String paramString1, @Param("pinNo") String paramString2, @Param("expiryTime") Timestamp paramTimestamp);

    @Modifying
    @Query(value = "update {h-schema}mobile_users set date_approved = :date_approved,  approved_by = :approved_by, approval_status = :approval_status, pin = :pinNo, pin_change_flag = :pin_change_flag, locked_flag = false, failed_login_count = 0, wap_otp_expiry = :expiryTime where phone_number = :phoneNo", nativeQuery = true)
    void approveMobileUser(@Param("phoneNo") String paramString1, @Param("pinNo") String paramString2, @Param("pin_change_flag") Boolean paramBoolean1, @Param("approval_status") Boolean paramBoolean2, @Param("approved_by") String paramString3, @Param("date_approved") Timestamp paramTimestamp1, @Param("expiryTime") Timestamp paramTimestamp2);

    @Modifying
    @Query(value = "update {h-schema}mobile_users set pin = :pinNo, pin_change_flag = true, locked_flag = false, failed_login_count = 0, wap_otp_expiry = :expiryTime where id = :id", nativeQuery = true)
    void changePin(@Param("id") Integer paramInteger, @Param("pinNo") String paramString, @Param("expiryTime") Timestamp paramTimestamp);

    @Modifying
    @Query(value = "update {h-schema}mobile_users set auth_imsi = :auth_imsi, auth_imei = :auth_imei where phone_number = :phoneNo", nativeQuery = true)
    void pairCustomerDevice(@Param("phoneNo") String paramString1, @Param("auth_imsi") String paramString2, @Param("auth_imei") String paramString3);

    @Query(value = "select count(*) from  {h-schema}mobile_users u where u.entity_code = :entity_code", nativeQuery = true)
    Integer findOutletCount(@Param("entity_code") Integer paramInteger);
}