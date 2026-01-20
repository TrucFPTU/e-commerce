package com.groupproject.ecommerce.repository;

import com.groupproject.ecommerce.entity.PaymentCallbackLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCallbackLogRepository extends JpaRepository<PaymentCallbackLog, Long> {

    Page<PaymentCallbackLog> findByProviderIgnoreCase(String provider, Pageable pageable);

    Page<PaymentCallbackLog> findByTxnRefContainingIgnoreCase(String txnRef, Pageable pageable);

    Page<PaymentCallbackLog> findByProviderIgnoreCaseAndTxnRefContainingIgnoreCase(
            String provider, String txnRef, Pageable pageable
    );
}
