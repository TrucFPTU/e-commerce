package com.groupproject.ecommerce.jobs;

import com.groupproject.ecommerce.service.inter.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderAutoCompleteJob {

    private final OrderService orderService;

    @Scheduled(fixedDelay = 10_000) // 10_000  /5 * 60 * 10005 phút/lần
    public void run() {
        orderService.autoCompleteShippedOrders();
    }
}