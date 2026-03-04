package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.Attachment;
import com.groupproject.ecommerce.repository.AttachmentRepo;
import com.groupproject.ecommerce.service.inter.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepo attachmentRepo;

    @Override
    public Attachment getAttachmentOrThrow(Long id) {
        return attachmentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));
    }

    @Override
    public Attachment getAttachmentByMessageIdOrThrow(Long messageId) {
        return attachmentRepo.findByMessage_Id(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));
    }
}