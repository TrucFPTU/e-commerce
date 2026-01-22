package com.groupproject.ecommerce.controller;


import com.groupproject.ecommerce.dto.response.StaffConversationSummaryResponse;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.Role;

import com.groupproject.ecommerce.service.inter.ChatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/staff/chat/conversations")
public class StaffChatConversationApiController {

    private final ChatService chatService;

    private User requireStaff(HttpSession session) {
        User u = (User) session.getAttribute("LOGIN_USER");
        if (u == null) return null;
        if (u.getRole() != Role.STAFF) return null;
        return u;
    }

    @GetMapping("/summary")
    public StaffConversationSummaryResponse summary(HttpSession session,
                                                    @RequestParam Long conversationId) {
        User staff = requireStaff(session);
        if (staff == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not staff");

        return chatService.getConversationSummaryForStaff(conversationId, staff.getUserId());
    }
}
