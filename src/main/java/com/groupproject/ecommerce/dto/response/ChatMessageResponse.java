package com.groupproject.ecommerce.dto.response;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageResponse {
    private Long conversationId;
    private Long messageId;

    private Long senderId;
    private String senderName;

    private String type;
    private String content;        // text hoặc caption
    private String attachmentUrl;
    // null nếu TEXT

    private String createdAt;
    private String originalName;
    private String mimeType;
    private Long size;// ISO string
}
