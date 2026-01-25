// dto/response/BookCardRes.java
package com.groupproject.ecommerce.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BookCardRes {
    private Long id;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private Integer publishYear;
    private Integer stock;
    private String authorName;
    private String publisherName;
    private String description;
    private String supplierName;
    private Long publisherId;
    private Long authorId;
    private Long supplierId;
}
