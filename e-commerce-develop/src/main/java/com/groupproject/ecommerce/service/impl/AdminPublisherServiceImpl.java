package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.Publisher;
import com.groupproject.ecommerce.repository.PublisherRepository;
import com.groupproject.ecommerce.service.inter.AdminPublisherService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class AdminPublisherServiceImpl implements AdminPublisherService {
    private final PublisherRepository repo;

    public AdminPublisherServiceImpl(PublisherRepository repo) {
        this.repo = repo;
    }
    @Override
    public List<Publisher> list() {
        return repo.findAll();
    }

    @Override
    public void save(Long publisherId, String name) {
        Publisher p = (publisherId == null) ? new Publisher() : repo.findById(publisherId).orElseThrow();
        p.setName(name);
        repo.save(p);
    }

    @Override
    public void delete(Long publisherId) {
        repo.deleteById(publisherId);
    }
}
