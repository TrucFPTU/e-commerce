package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.Author;
import com.groupproject.ecommerce.repository.AuthorRepository;
import com.groupproject.ecommerce.service.inter.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    @Override
    public Author getById(Long id) {
        Author a = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found: " + id));
        return a;
    }
}
