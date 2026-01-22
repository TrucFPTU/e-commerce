package com.groupproject.ecommerce.dto.response;


import lombok.AllArgsConstructor;
import lombok.*;

@Data
@AllArgsConstructor
@Getter
@Setter
public class StaffConversationSummaryResponse {
    private Long conversationId;
    private Long customerId;
    private String customerName;
}