package com.smartlibrary.service;

import com.smartlibrary.dto.CategoryRequestDTO;
import com.smartlibrary.dto.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {

    List<CategoryResponseDTO> getAllCategories();

    CategoryResponseDTO getCategoryById(Long categoryId);

    CategoryResponseDTO addCategory(CategoryRequestDTO request);

    CategoryResponseDTO updateCategory(Long categoryId, CategoryRequestDTO request);

    void deleteCategory(Long categoryId);
}
