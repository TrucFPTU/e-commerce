package com.groupproject.ecommerce.repository;

import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.entity.PaymentTransaction;
import com.groupproject.ecommerce.enums.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    
    Optional<PaymentTransaction> findByTxnRef(String txnRef);
    
    List<PaymentTransaction> findByOrder(Order order);
    
    Optional<PaymentTransaction> findByOrderAndProvider(Order order, PaymentProvider provider);
    
    List<PaymentTransaction> findByOrderOrderByCreatedAtDesc(Order order);
}
