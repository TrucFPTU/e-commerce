package com.groupproject.ecommerce.repository;

import com.groupproject.ecommerce.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepo extends JpaRepository<Message, Long> {

    Page<Message> findByConversation_IdOrderByIdDesc(Long conversationId, Pageable pageable);

    Page<Message> findByConversation_IdAndIdLessThanOrderByIdDesc(Long conversationId, Long beforeId, Pageable pageable);
}
