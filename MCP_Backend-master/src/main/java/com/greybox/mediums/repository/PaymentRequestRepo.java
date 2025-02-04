package com.greybox.mediums.repository;

import com.greybox.mediums.entities.PaymentRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRequestRepo extends JpaRepository<PaymentRequest, Integer> {
    @Query(value = "select u.* from {h-schema}payment_request u where u.from_phone = ?1 and u.status = 'Pending'", nativeQuery = true)
    List<PaymentRequest> findPendingRequest(String paramString);

    @Query(value = "select u.* from {h-schema}payment_request u where u.requester_phone = ?1", nativeQuery = true)
    List<PaymentRequest> findRequestedPayments(String paramString);

    @Query(value = "select u.* from {h-schema}payment_request u where u.payment_rqst_id = ?1", nativeQuery = true)
    PaymentRequest findRequestedPayments(Integer paramInteger);
}