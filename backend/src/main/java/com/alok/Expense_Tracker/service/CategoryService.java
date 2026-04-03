package com.alok.Expense_Tracker.service;

import com.alok.Expense_Tracker.dto.CategoryRequestDto;
import com.alok.Expense_Tracker.dto.CategoryResponseDto;
import com.alok.Expense_Tracker.entity.type.CategoryType;

import java.util.List;
import java.util.Set;

public interface CategoryService {

     CategoryResponseDto createCategory(CategoryRequestDto categoryRequesteDtoDto);

     List<CategoryResponseDto> findAllCategories();

     CategoryResponseDto updateCategory(Long id,CategoryRequestDto categoryRequesteDtoDto);

     void deleteCategories(Long id);

     List<CategoryResponseDto> findUserandCategoryType(CategoryType type);

     CategoryResponseDto findAllCategoriesById(Long id);
}
