package com.greybox.mediums.repository;

import com.greybox.mediums.entities.ServicePostingDetail;
import com.greybox.mediums.entities.TransactionVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public interface TransactionVoucherRepo extends JpaRepository<TransactionVoucher, Long> {
    @Query(value = "select u.* from  {h-schema}transaction_voucher u where u.voucher_code = :voucher_code and u.recipient_phone_no = :recipient_phone_no", nativeQuery = true)
    TransactionVoucher findVoucherByRecipientPhone(
            @Param("voucher_code") String voucherNo,
            @Param("recipient_phone_no") String recipientPhoneNo
    );

    @Query(value = "select u.* from  {h-schema}transaction_voucher u where u.voucher_code = :voucher_code and u.recipient_phone_no = :recipient_phone_no and  u.status = 'PENDING'", nativeQuery = true)
    TransactionVoucher findActiveVoucher(
            @Param("voucher_code") String voucherNo,
            @Param("recipient_phone_no") String recipientPhoneNo
    );

    @Query(value = "select u.* from  {h-schema}transaction_voucher u where u.status = 'PENDING'", nativeQuery = true)
    ArrayList<TransactionVoucher> findActiveVouchers();


    @Query(value = "select u.* from  {h-schema}transaction_voucher u order by u.voucher_id desc", nativeQuery = true)
    List<TransactionVoucher> findTransVouchers();

    @Modifying
    @Query(value = "update {h-schema}transaction_voucher set status = :status where voucher_code = :voucher_code and recipient_phone_no = :recipient_phone_no", nativeQuery = true)
    void updateVoucher(@Param("voucher_code") String voucherCode,
                          @Param("status") String status,
                          @Param("recipient_phone_no") String recipientPhoneNo);


}