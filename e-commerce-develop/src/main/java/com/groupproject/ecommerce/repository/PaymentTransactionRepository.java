package com.groupproject.ecommerce.repository;

import com.groupproject.ecommerce.dto.response.AdminPaymentTransactionListItemResponse;
import com.groupproject.ecommerce.entity.PaymentTransaction;
import com.groupproject.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    @Query("""
        select new com.groupproject.ecommerce.dto.response.AdminPaymentTransactionListItemResponse(
            p.paymentId,
            o.orderId,
            p.provider,
            p.txnRef,
            p.amount,
            p.currency,
            p.status,
            p.responseCode,
            p.transactionNo,
            p.createdAt,
            p.payDate
        )
        from PaymentTransaction p
        join p.order o
        where (:status is null or p.status = :status)
          and (:provider is null or p.provider = :provider)
        order by p.createdAt desc
    """)
    Page<AdminPaymentTransactionListItemResponse> adminList(
            @Param("status") com.groupproject.ecommerce.enums.PaymentStatus status,
            @Param("provider") com.groupproject.ecommerce.enums.PaymentProvider provider,
            Pageable pageable
    );
}
