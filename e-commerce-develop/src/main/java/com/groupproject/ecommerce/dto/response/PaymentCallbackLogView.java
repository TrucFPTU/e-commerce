package com.groupproject.ecommerce.dto.response;

import com.groupproject.ecommerce.enums.PaymentProvider;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCallbackLogView {
    private Long logId;
    private PaymentProvider provider;
    private String txnRef;
    private LocalDateTime receivedAt;
    private Boolean validSignature;
    private String clientIp;
    private String rawPayload;

    // field để view hiển thị, có thể null nếu bạn chưa lưu trong DB
    private Integer httpStatus;
}
