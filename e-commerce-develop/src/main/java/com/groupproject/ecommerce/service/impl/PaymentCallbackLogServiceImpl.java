package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.PaymentCallbackLog;
import com.groupproject.ecommerce.repository.PaymentCallbackLogRepository;
import com.groupproject.ecommerce.service.inter.PaymentCallbackLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentCallbackLogServiceImpl implements PaymentCallbackLogService {

    private final PaymentCallbackLogRepository repo;

    @Override
    public Page<PaymentCallbackLog> getLogs(String provider, String txnRef, Pageable pageable) {
        boolean hasProvider = provider != null && !provider.isBlank();
        boolean hasTxnRef = txnRef != null && !txnRef.isBlank();

        if (hasProvider && hasTxnRef) {
            return repo.findByProviderIgnoreCaseAndTxnRefContainingIgnoreCase(provider.trim(), txnRef.trim(), pageable);
        }
        if (hasProvider) {
            return repo.findByProviderIgnoreCase(provider.trim(), pageable);
        }
        if (hasTxnRef) {
            return repo.findByTxnRefContainingIgnoreCase(txnRef.trim(), pageable);
        }
        return repo.findAll(pageable);
    }

    @Override
    public PaymentCallbackLog getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("PaymentCallbackLog not found: " + id));
    }
}
