package com.groupproject.ecommerce.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSendRequest {
    private Long conversationId;
    private String text;
}
