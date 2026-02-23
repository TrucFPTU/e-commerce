package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.entity.Attachment;

public interface AttachmentService {

    Attachment getAttachmentOrThrow(Long id);

    Attachment getAttachmentByMessageIdOrThrow(Long messageId);
}