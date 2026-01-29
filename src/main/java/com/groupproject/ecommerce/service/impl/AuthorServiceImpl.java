package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.Author;
import com.groupproject.ecommerce.repository.AuthorRepository;
import com.groupproject.ecommerce.service.inter.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for Author management
 */
@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    @Override
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    @Override
    public Author getById(Long id) {
        return authorRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Author saveAuthor(Long authorId, String name) {
        Author author;
        
        if (authorId != null) {
            author = authorRepository.findById(authorId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tác giả"));
        } else {
            author = new Author();
        }
        
        author.setName(name);
        return authorRepository.save(author);
    }

    @Override
    @Transactional
    public void deleteAuthor(Long id) {
        authorRepository.deleteById(id);
    }
}
