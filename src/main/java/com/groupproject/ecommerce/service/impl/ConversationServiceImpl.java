package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.Conversation;
import com.groupproject.ecommerce.enums.ConversationStatus;
import com.groupproject.ecommerce.repository.ConversationRepo;
import com.groupproject.ecommerce.service.inter.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepo conversationRepo;

    @Override
    public Conversation getConversationOrThrow(Long conversationId) {
        return conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }

    @Override
    public List<Conversation> getConversationsByStaffAndStatusOrderByLastMessageAtDesc(Long staffId, ConversationStatus status) {
        return conversationRepo.findByStaff_UserIdAndStatusOrderByLastMessageAtDesc(staffId, status);
    }

    @Override
    public Page<Conversation> getConversationsByStaffAndStatus(Long staffId, ConversationStatus status, Pageable pageable) {
        return conversationRepo.findByStaff_UserIdAndStatus(staffId, status, pageable);
    }

    @Override
    public Page<Conversation> getConversationsByStatus(ConversationStatus status, Pageable pageable) {
        return conversationRepo.findByStatus(status, pageable);
    }

    @Override
    public long countByStaffAndStatus(Long staffId, ConversationStatus status) {
        return conversationRepo.countByStaff_UserIdAndStatus(staffId, status);
    }
}