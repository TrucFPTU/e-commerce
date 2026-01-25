package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.Publisher;
import com.groupproject.ecommerce.repository.PublisherRepository;
import com.groupproject.ecommerce.service.inter.PublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;

    @Override
    public Publisher getById(Long id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publisher not found: " + id));
    }
}
