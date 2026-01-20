package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.Author;
import com.groupproject.ecommerce.repository.AuthorRepository;
import com.groupproject.ecommerce.service.inter.AdminAuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class AdminAuthorServiceImpl implements AdminAuthorService {

    private final AuthorRepository repo;

    public AdminAuthorServiceImpl(AuthorRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Author> list() {
        return repo.findByDeletedAtIsNullOrderByAuthorIdDesc();
    }

    @Override
    public void save(Long authorId, String name) {
        Author a = (authorId == null) ? new Author() : repo.findById(authorId).orElseThrow();

        a.setName(name);
        repo.save(a);
    }

    @Override
    public void softDelete(Long authorId) {
        Author a = repo.findById(authorId).orElseThrow();
        a.setDeletedAt(java.time.LocalDateTime.now());
        repo.save(a);
    }


}
