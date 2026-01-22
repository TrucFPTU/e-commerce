package com.groupproject.ecommerce.repository;

import com.groupproject.ecommerce.entity.Conversation;
import com.groupproject.ecommerce.enums.ConversationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository

public interface ConversationRepo extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByCustomer_UserIdAndStatus(Long customerId, ConversationStatus status);

    boolean existsByCustomer_UserIdAndStatus(Long customerId, ConversationStatus status);
    List<Conversation> findByStaff_UserIdAndStatusOrderByLastMessageAtDesc(Long staffId, ConversationStatus status);
    Page<Conversation> findByStaff_UserIdAndStatus(Long staffId, ConversationStatus status, Pageable pageable);
    Page<Conversation> findByStatus(ConversationStatus status, Pageable pageable);
    long countByStaff_UserIdAndStatus(Long staffId, ConversationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conversation c where c.customer.userId = :customerId and c.status = :status")
    Optional<Conversation> findOpenByCustomerForUpdate(@Param("customerId") Long customerId,
                                                       @Param("status") ConversationStatus status);
}
