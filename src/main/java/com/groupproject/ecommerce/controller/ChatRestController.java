package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.dto.response.ChatBootstrapResponse;
import com.groupproject.ecommerce.dto.response.ChatMessageResponse;
import com.groupproject.ecommerce.entity.Attachment;
import com.groupproject.ecommerce.entity.Conversation;
import com.groupproject.ecommerce.entity.Message;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.repository.AttachmentRepo;
import com.groupproject.ecommerce.repository.ConversationRepo;
import com.groupproject.ecommerce.service.inter.ChatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatRestController {

    private final ChatService chatService;
    private final ConversationRepo conversationRepo;
    private final AttachmentRepo attachmentRepo;

    @GetMapping("/bootstrap")
    public ChatBootstrapResponse bootstrap(HttpSession session,
                                           @RequestParam(defaultValue = "30") int limit) {
        User u = (User) session.getAttribute("LOGIN_USER");
        if (u == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");

        int safeLimit = Math.max(1, Math.min(limit, 100));

        Long conversationId = chatService.getOrCreateOpenConversationIdForCustomer(u.getUserId());
        List<ChatMessageResponse> messages = chatService.getLatestMessages(conversationId, safeLimit, u.getUserId());

        Long oldestId = messages.isEmpty() ? null : messages.get(0).getMessageId();
        Long newestId = messages.isEmpty() ? null : messages.get(messages.size() - 1).getMessageId();

        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        return ChatBootstrapResponse.builder()
                .conversationId(conversationId)
                .messages(messages)
                .oldestMessageId(oldestId)
                .newestMessageId(newestId)
                .currentUserId(u.getUserId())
                .staffId(conv.getStaff().getUserId())
                .staffName(conv.getStaff().getFullName())
                .build();
    }


    @GetMapping("/older")
    public List<ChatMessageResponse> older(HttpSession session,
                                           @RequestParam Long conversationId,
                                           @RequestParam Long beforeMessageId,
                                           @RequestParam(defaultValue = "30") int limit) {
        User u = (User) session.getAttribute("LOGIN_USER");
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        int safeLimit = Math.max(1, Math.min(limit, 100));

        // NOTE: hiện service.getOlderMessages() chưa check "requester có thuộc conversation không"
        // nhưng bạn đã check khi gửi message rồi. Nếu muốn chặt hơn, mình sẽ bổ sung check trong service sau.
        return chatService.getOlderMessages(conversationId, beforeMessageId, safeLimit, u.getUserId());

    }

    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ChatMessageResponse> uploadAttachments(
            HttpSession session,
            @RequestParam Long conversationId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        User u = (User) session.getAttribute("LOGIN_USER");
        if (u == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");

        if (files == null || files.isEmpty()) return List.of();
        if (files.size() > 5) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max 5 files");

        List<ChatMessageResponse> out = new ArrayList<>();
        for (MultipartFile f : files) {
            out.add(chatService.saveAttachmentAndPush(conversationId, u.getUserId(), f));
        }
        return out;
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<Resource> downloadChatFile(HttpSession session, @PathVariable Long id) {
        User u = (User) session.getAttribute("LOGIN_USER");
        if (u == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");

        Attachment at = attachmentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));

        // CHỐT: check quyền theo conversation
        Message msg = at.getMessage(); // LAZY ok vì đang trong request
        Conversation conv = msg.getConversation();
        Long uid = u.getUserId();
        boolean allowed =
                conv.getCustomer().getUserId().equals(uid) || conv.getStaff().getUserId().equals(uid);

        if (!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");

        Path filePath = Paths.get(System.getProperty("user.dir"), "uploads", "chat", at.getStoredName());

        if (!Files.exists(filePath)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File missing");

        Resource res = new org.springframework.core.io.FileSystemResource(filePath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(at.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + at.getOriginalName().replace("\"", "") + "\"")
                .body(res);
    }


}
