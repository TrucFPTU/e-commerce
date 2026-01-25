package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.dto.response.BookCardRes;
import java.util.List;

public interface ProductService {
    List<BookCardRes> getHomeBooks();
    BookCardRes getBookCardById(Long id);
    List<BookCardRes> getRelatedBooks(Long productId);
    List<BookCardRes> getBooksByAuthor(Long authorId);
    List<BookCardRes> getBooksByPublisher(Long publisherId);
    List<BookCardRes> getBooksBySupplier(Long supplierId);


}
