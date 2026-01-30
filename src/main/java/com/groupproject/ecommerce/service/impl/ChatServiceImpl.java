package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.dto.response.ChatMessageResponse;
import com.groupproject.ecommerce.dto.response.StaffConversationSummaryResponse;
import com.groupproject.ecommerce.entity.Attachment;
import com.groupproject.ecommerce.entity.Conversation;
import com.groupproject.ecommerce.entity.Message;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.ConversationStatus;
import com.groupproject.ecommerce.enums.MessageType;
import com.groupproject.ecommerce.enums.Role;
import com.groupproject.ecommerce.repository.AttachmentRepo;
import com.groupproject.ecommerce.repository.ConversationRepo;
import com.groupproject.ecommerce.repository.MessageRepo;
import com.groupproject.ecommerce.repository.UserRepository;
import com.groupproject.ecommerce.service.inter.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepo conversationRepo;
    private final MessageRepo messageRepo;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AttachmentRepo attachmentRepo;
    private final ConversationRepo conversationRepository;
    private final MinioService minioService;



    @Override
    @Transactional
    public Long getOrCreateOpenConversationIdForCustomer(Long customerId) {

        Optional<Conversation> existing = conversationRepo.findOpenByCustomerForUpdate(customerId, ConversationStatus.OPEN);
        if (existing.isPresent()) return existing.get().getId();

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + customerId));

        if (customer.getRole() != Role.CUSTOMER) {
            throw new IllegalStateException("Only CUSTOMER can create customer chat conversation");
        }

        Long staffId = pickRandomStaffId();
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new NoSuchElementException("Staff not found: " + staffId));

        Conversation c = new Conversation();
        c.setCustomer(customer);
        c.setStaff(staff);
        c.setStatus(ConversationStatus.OPEN);
        c.setCreatedAt(Instant.now());
        c.setLastMessageAt(Instant.now());

        Conversation saved = conversationRepo.save(c);
        return saved.getId();
    }

    private Long pickRandomStaffId() {

        List<Long> staffIds = userRepository.findAllStaffIds();
        if (staffIds == null || staffIds.isEmpty()) {
            throw new IllegalStateException("No STAFF available to assign");
        }
        int idx = new Random().nextInt(staffIds.size());
        return staffIds.get(idx);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getLatestMessages(Long conversationId, int limit, Long requesterId) {

        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));

        validateRequesterInConversation(conv, requesterId);

        var page = messageRepo.findByConversation_IdOrderByIdDesc(conversationId, PageRequest.of(0, limit));

        List<Message> desc = new ArrayList<>(page.getContent());
        Collections.reverse(desc);

        return desc.stream().map(this::toResponse).collect(Collectors.toList());

    }

    private void validateRequesterInConversation(Conversation conv, Long requesterId) {
        Long customerId = conv.getCustomer().getUserId();
        Long staffId = conv.getStaff().getUserId();

        if (!Objects.equals(requesterId, customerId) && !Objects.equals(requesterId, staffId)) {
            throw new SecurityException("Not allowed");
        }
    }



    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getOlderMessages(Long conversationId, Long beforeMessageId, int limit, Long requesterId) {

        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));

        validateRequesterInConversation(conv, requesterId);

        var page = messageRepo.findByConversation_IdAndIdLessThanOrderByIdDesc(
                conversationId, beforeMessageId, PageRequest.of(0, limit)
        );

        List<Message> desc = new ArrayList<>(page.getContent());
        Collections.reverse(desc);

        return desc.stream().map(this::toResponse).collect(Collectors.toList());

    }

    @Override
    @Transactional
    public ChatMessageResponse saveTextAndPush(Long conversationId, Long senderId, String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Message text cannot be blank");
        }

        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));

        validateSenderInConversation(conv, senderId);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new NoSuchElementException("Sender not found: " + senderId));

        Message m = new Message();
        m.setConversation(conv);
        m.setSender(sender);
        m.setType(MessageType.TEXT);
        m.setContent(text.trim());
        m.setCreatedAt(Instant.now());

        Message saved = messageRepo.save(m);

        conv.setLastMessageAt(saved.getCreatedAt());
        conversationRepo.save(conv);

        ChatMessageResponse resp = toResponse(saved);

        Long customerId = conv.getCustomer().getUserId();
        Long staffId = conv.getStaff().getUserId();

        messagingTemplate.convertAndSendToUser(customerId.toString(), "/queue/messages", resp);
        messagingTemplate.convertAndSendToUser(staffId.toString(), "/queue/messages", resp);

        return resp;
    }

    private void validateSenderInConversation(Conversation conv, Long senderId) {
        Long customerId = conv.getCustomer().getUserId();
        Long staffId = conv.getStaff().getUserId();

        if (!Objects.equals(senderId, customerId) && !Objects.equals(senderId, staffId)) {
            throw new SecurityException("Sender is not a participant of this conversation");
        }

        if (conv.getStatus() != ConversationStatus.OPEN) {
            throw new IllegalStateException("Conversation is not OPEN");
        }
    }

    private ChatMessageResponse toResponse(Message m) {
        String type = (m.getType() != null ? m.getType().name() : "TEXT");

        String attachmentUrl = null;
        if ("IMAGE".equals(type) || "FILE".equals(type)) {
            attachmentUrl = attachmentRepo.findByMessage_Id(m.getId())
                    .map(Attachment::getUrl)
                    .orElse(null);
        }

        return ChatMessageResponse.builder()
                .conversationId(m.getConversation().getId())
                .messageId(m.getId())
                .senderId(m.getSender().getUserId())
                .senderName(m.getSender().getFullName())
                .type(type)
                .content(m.getContent())
                .attachmentUrl(attachmentUrl)
                .createdAt(m.getCreatedAt() != null ? m.getCreatedAt().toString() : null)
                .build();
    }


    @Override
    @Transactional
    public ChatMessageResponse saveAttachmentAndPush(Long conversationId, Long senderId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));

        validateSenderInConversation(conv, senderId);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new NoSuchElementException("Sender not found: " + senderId));

        String mime = (file.getContentType() != null) ? file.getContentType() : "application/octet-stream";
        boolean isImage = mime.toLowerCase().startsWith("image/");

        Message m = new Message();
        m.setConversation(conv);
        m.setSender(sender);
        m.setType(isImage ? MessageType.IMAGE : MessageType.FILE);
        m.setContent(file.getOriginalFilename());
        m.setCreatedAt(Instant.now());
        Message savedMsg = messageRepo.save(m);

        String original = (file.getOriginalFilename() != null) ? file.getOriginalFilename() : "file";
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) ext = original.substring(dot);

        String storedName = "chat/" + UUID.randomUUID() + ext;

        try {
            minioService.uploadFile(storedName, file);
        } catch (Exception e) {
            throw new RuntimeException("Cannot save file to MinIO", e);
        }

        Attachment at = new Attachment();
        at.setMessage(savedMsg);
        at.setOriginalName(original);
        at.setStoredName(storedName);
        at.setMimeType(mime);
        at.setSize(file.getSize());
        at.setUrl("PENDING");
        Attachment savedAt = attachmentRepo.save(at);


        savedAt.setUrl("/chat/file/" + savedAt.getId());
        attachmentRepo.save(savedAt);
        conv.setLastMessageAt(savedMsg.getCreatedAt());
        conversationRepo.save(conv);

        ChatMessageResponse resp = toResponse(savedMsg);
        Long customerId = conv.getCustomer().getUserId();
        Long staffId = conv.getStaff().getUserId();
        messagingTemplate.convertAndSendToUser(customerId.toString(), "/queue/messages", resp);
        messagingTemplate.convertAndSendToUser(staffId.toString(), "/queue/messages", resp);

        return resp;
    }


    @Override
    @Transactional(readOnly = true)
    public StaffConversationSummaryResponse getConversationSummaryForStaff(Long conversationId, Long staffId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));

        if (conv.getStaff() == null || !Objects.equals(conv.getStaff().getUserId(), staffId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your conversation");
        }

        User customer = conv.getCustomer();
        return new StaffConversationSummaryResponse(
                conv.getId(),
                customer != null ? customer.getUserId() : null,
                customer != null ? customer.getFullName() : "Khách hàng"
        );
    }



}
