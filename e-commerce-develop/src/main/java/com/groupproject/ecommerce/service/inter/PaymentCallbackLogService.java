package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.entity.PaymentCallbackLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentCallbackLogService {

    Page<PaymentCallbackLog> getLogs(String provider, String txnRef, Pageable pageable);

    PaymentCallbackLog getById(Long id); // optional: xem chi tiết 1 log
}
