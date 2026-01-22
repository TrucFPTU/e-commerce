package com.groupproject.ecommerce.controller;


import com.groupproject.ecommerce.dto.request.ChatSendRequest;
import com.groupproject.ecommerce.service.inter.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void send(ChatSendRequest req, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("Unauthenticated websocket connection");
        }
        Long senderId = Long.parseLong(principal.getName()); // vì handshake set userId rồi
        chatService.saveTextAndPush(req.getConversationId(), senderId, req.getText());
    }

}
