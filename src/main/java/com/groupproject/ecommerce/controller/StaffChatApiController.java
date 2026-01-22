package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.dto.response.ChatMessageResponse;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.Role;
import com.groupproject.ecommerce.service.inter.ChatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/staff/chat/messages")
public class StaffChatApiController {

    private final ChatService chatService;

    private User requireStaff(HttpSession session) {
        User u = (User) session.getAttribute("LOGIN_USER");
        if (u == null) return null;
        if (u.getRole() != Role.STAFF) return null;
        return u;
    }

    @GetMapping("/latest")
    public List<ChatMessageResponse> latest(HttpSession session,
                                            @RequestParam Long conversationId,
                                            @RequestParam(defaultValue = "30") int limit) {
        User staff = requireStaff(session);
        if (staff == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not staff");

        int safeLimit = Math.max(1, Math.min(limit, 100));
        // ✅ Nên check staff có thuộc conversation không (mình sẽ bổ sung ở service bên dưới)
        return chatService.getLatestMessages(conversationId, safeLimit, staff.getUserId());
    }

    @GetMapping("/older")
    public List<ChatMessageResponse> older(HttpSession session,
                                           @RequestParam Long conversationId,
                                           @RequestParam Long beforeMessageId,
                                           @RequestParam(defaultValue = "30") int limit) {
        User staff = requireStaff(session);
        if (staff == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not staff");

        int safeLimit = Math.max(1, Math.min(limit, 100));
        return chatService.getOlderMessages(conversationId, beforeMessageId, safeLimit, staff.getUserId());
    }

    @PostMapping(value = "/staff/chat/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ChatMessageResponse> uploadStaffAttachments(
            HttpSession session,
            @RequestParam Long conversationId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        User u = (User) session.getAttribute("LOGIN_USER");
        if (u == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");

        // optional: check role staff
        // if (u.getRole() != Role.STAFF) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        if (files == null || files.isEmpty()) return List.of();
        if (files.size() > 5) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max 5 files");

        List<ChatMessageResponse> out = new ArrayList<>();
        for (MultipartFile f : files) {
            out.add(chatService.saveAttachmentAndPush(conversationId, u.getUserId(), f));
        }
        return out;
    }

}
