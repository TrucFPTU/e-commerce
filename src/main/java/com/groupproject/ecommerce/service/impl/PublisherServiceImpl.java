package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.Publisher;
import com.groupproject.ecommerce.repository.PublisherRepository;
import com.groupproject.ecommerce.service.inter.PublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for Publisher management
 */
@Service
@RequiredArgsConstructor
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;

    @Override
    public List<Publisher> getAllPublishers() {
        return publisherRepository.findAll();
    }

    @Override
    public Publisher getById(Long id) {
        return publisherRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Publisher savePublisher(Long publisherId, String name) {
        Publisher publisher;
        
        if (publisherId != null) {
            publisher = publisherRepository.findById(publisherId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà xuất bản"));
        } else {
            publisher = new Publisher();
        }
        
        publisher.setName(name);
        return publisherRepository.save(publisher);
    }

    @Override
    @Transactional
    public void deletePublisher(Long id) {
        publisherRepository.deleteById(id);
    }
}
