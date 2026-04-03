package com.alok.Expense_Tracker.controller;

import com.alok.Expense_Tracker.dto.CategoryRequestDto;
import com.alok.Expense_Tracker.dto.CategoryResponseDto;
import com.alok.Expense_Tracker.dto.CategorySummaryDto;
import com.alok.Expense_Tracker.entity.Category;
import com.alok.Expense_Tracker.entity.type.CategoryType;
import com.alok.Expense_Tracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

private final CategoryService categoryService;
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryResponseDto> CreateCategory(@RequestBody CategoryRequestDto categoryRequestDto){

        return ResponseEntity.ok(categoryService.createCategory(categoryRequestDto));

    }
    @PreAuthorize("hasAnyRole('USER','ADMIN','AUDITOR')")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories(){
        List<CategoryResponseDto> categories=categoryService.findAllCategories();
        return ResponseEntity.ok(categories) ;
    }
    @PreAuthorize("hasAnyRole('USER','ADMIN','AUDITOR')")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCateegoriesByid(@PathVariable Long id){
        return ResponseEntity.ok(categoryService.findAllCategoriesById(id));

    }
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategories(@PathVariable Long id,@RequestBody CategoryRequestDto categoryRequestDto){
        return ResponseEntity.ok(categoryService.updateCategory(id,categoryRequestDto));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteCategories(@PathVariable Long id){
        categoryService.deleteCategories(id);

    }
    @PreAuthorize("hasAnyRole('USER','ADMIN','AUDITOR')")
    @GetMapping("/type/{type}")
    public ResponseEntity<List<CategoryResponseDto>> findUserAndCategoryType(@PathVariable CategoryType type){
        List<CategoryResponseDto> categories=categoryService.findUserandCategoryType(type);
        return ResponseEntity.ok(categories);
    }

}
