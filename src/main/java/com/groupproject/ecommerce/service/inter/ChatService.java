package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.dto.response.ChatMessageResponse;
import com.groupproject.ecommerce.dto.response.StaffConversationSummaryResponse;

import java.util.List;

public interface ChatService {

    Long getOrCreateOpenConversationIdForCustomer(Long customerId);

    List<ChatMessageResponse> getLatestMessages(Long conversationId, int limit, Long requesterId);

    List<ChatMessageResponse> getOlderMessages(Long conversationId, Long beforeMessageId, int limit, Long requesterId);

    ChatMessageResponse saveTextAndPush(Long conversationId, Long senderId, String text);

    ChatMessageResponse saveAttachmentAndPush(Long conversationId, Long senderId, org.springframework.web.multipart.MultipartFile file);


    StaffConversationSummaryResponse getConversationSummaryForStaff(Long conversationId, Long staffId);


}
