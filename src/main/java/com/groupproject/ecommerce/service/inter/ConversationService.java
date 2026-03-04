package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.entity.Conversation;
import com.groupproject.ecommerce.enums.ConversationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ConversationService {

    Conversation getConversationOrThrow(Long conversationId);

    List<Conversation> getConversationsByStaffAndStatusOrderByLastMessageAtDesc(Long staffId, ConversationStatus status);

    Page<Conversation> getConversationsByStaffAndStatus(Long staffId, ConversationStatus status, Pageable pageable);

    Page<Conversation> getConversationsByStatus(ConversationStatus status, Pageable pageable);

    long countByStaffAndStatus(Long staffId, ConversationStatus status);
}