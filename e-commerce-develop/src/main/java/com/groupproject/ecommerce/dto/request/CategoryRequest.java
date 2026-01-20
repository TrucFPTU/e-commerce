package com.groupproject.ecommerce.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {
    private Long categoryId;
    private String name;
    private Long parentId;
}
