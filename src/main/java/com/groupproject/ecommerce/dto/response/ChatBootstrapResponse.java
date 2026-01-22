package com.groupproject.ecommerce.dto.response;



import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatBootstrapResponse {
    private Long conversationId;
    private List<ChatMessageResponse> messages;

    // phục vụ load older
    private Long oldestMessageId; // null nếu chưa có tin
    private Long newestMessageId; // null nếu chưa có tin
    private Long currentUserId;
    private Long staffId;       // thêm
    private String staffName;

}
