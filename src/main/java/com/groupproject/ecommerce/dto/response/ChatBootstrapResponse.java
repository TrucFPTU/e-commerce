package com.groupproject.ecommerce.dto.response;



import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatBootstrapResponse {
    private Long conversationId;
    private List<ChatMessageResponse> messages;
    private Long oldestMessageId;
    private Long newestMessageId;
    private Long currentUserId;
    private Long staffId;
    private String staffName;

}
