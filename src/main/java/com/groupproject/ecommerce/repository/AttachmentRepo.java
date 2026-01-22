package com.groupproject.ecommerce.repository;


import com.groupproject.ecommerce.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AttachmentRepo extends JpaRepository<Attachment, Long> {

    Optional<Attachment> findByMessage_Id(Long messageId);
}
