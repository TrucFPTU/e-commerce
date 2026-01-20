package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.entity.Publisher;

import java.util.List;

public interface AdminPublisherService {
    List<Publisher> list();
    void save(Long publisherId, String name);
    void delete(Long publisherId);
}
