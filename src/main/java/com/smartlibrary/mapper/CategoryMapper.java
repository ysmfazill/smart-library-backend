package com.smartlibrary.mapper;

import com.smartlibrary.dto.CategoryRequestDTO;
import com.smartlibrary.dto.CategoryResponseDTO;
import com.smartlibrary.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponseDTO toResponseDTO(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .totalBooks(category.getBooks() != null ? category.getBooks().size() : 0)
                .build();
    }

    public Category toEntity(CategoryRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }
}
