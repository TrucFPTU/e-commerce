package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.dto.response.AdminPaymentTransactionListItemResponse;
import com.groupproject.ecommerce.enums.PaymentProvider;
import com.groupproject.ecommerce.enums.PaymentStatus;
import org.springframework.data.domain.Page;

public interface AdminPaymentService {
    Page<AdminPaymentTransactionListItemResponse> list(PaymentStatus status, PaymentProvider provider, int page, int size);

}
