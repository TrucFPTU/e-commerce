package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.dto.response.AdminPaymentTransactionListItemResponse;
import com.groupproject.ecommerce.enums.PaymentProvider;
import com.groupproject.ecommerce.enums.PaymentStatus;
import com.groupproject.ecommerce.repository.PaymentTransactionRepository;
import com.groupproject.ecommerce.service.inter.AdminPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminPaymentServiceImpl implements AdminPaymentService {

    private final PaymentTransactionRepository paymentRepo;

    @Override
    public Page<AdminPaymentTransactionListItemResponse> list(PaymentStatus status, PaymentProvider provider, int page, int size) {
        return paymentRepo.adminList(status, provider, PageRequest.of(page, size));
    }
}

